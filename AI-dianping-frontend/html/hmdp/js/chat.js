(function () {
  const params = new URLSearchParams(location.search);
  const shopId = Number(params.get("shopId"));
  const token = sessionStorage.getItem("token");
  const messageBox = document.getElementById("chat-messages");
  const statusBox = document.getElementById("chat-status");
  const sendButton = document.getElementById("chat-send");
  const input = document.getElementById("chat-input");
  const customerPicker = document.getElementById("customer-picker");
  let currentUserId = null;
  let isMerchant = false;
  let socket = null;
  let stompBuffer = "";

  if (!token) {
    location.href = "/login.html";
    return;
  }
  if (!Number.isSafeInteger(shopId) || shopId <= 0) {
    statusBox.textContent = "店铺参数无效";
    return;
  }

  function escapeHtml(value) {
    return String(value).replace(/[&<>"']/g, function (ch) {
      return ({ "&": "&amp;", "<": "&lt;", ">": "&gt;", '"': "&quot;", "'": "&#39;" })[ch];
    });
  }

  function selectedCustomerId() {
    return isMerchant ? Number(customerPicker.value) : currentUserId;
  }

  function renderMessage(message) {
    if (Number(message.shopId) !== shopId || Number(message.customerUserId) !== selectedCustomerId()) return;
    const mine = Number(message.senderUserId) === currentUserId;
    const empty = messageBox.querySelector(".chat-empty");
    if (empty) empty.remove();
    const row = document.createElement("div");
    row.className = "chat-row" + (mine ? " mine" : "");
    const time = message.createTime ? new Date(message.createTime).toLocaleString() : "";
    row.innerHTML = '<div class="chat-bubble">' + escapeHtml(message.content) +
      '<div class="chat-meta">' + (message.senderRole === "merchant" ? "商家" : "顾客") + " · " + escapeHtml(time) + "</div></div>";
    messageBox.appendChild(row);
    messageBox.scrollTop = messageBox.scrollHeight;
  }

  function frame(command, headers, body) {
    let value = command + "\n";
    Object.keys(headers).forEach(function (key) { value += key + ":" + headers[key] + "\n"; });
    return value + "\n" + (body || "") + "\0";
  }

  function sendFrame(command, headers, body) {
    if (socket && socket.readyState === WebSocket.OPEN) socket.send(frame(command, headers || {}, body || ""));
  }

  function processFrame(raw) {
    const boundary = raw.indexOf("\n\n");
    if (boundary < 0) return;
    const lines = raw.substring(0, boundary).split("\n");
    const command = lines.shift();
    const headers = {};
    lines.forEach(function (line) {
      const idx = line.indexOf(":");
      if (idx > 0) headers[line.substring(0, idx)] = line.substring(idx + 1);
    });
    const body = raw.substring(boundary + 2);
    if (command === "CONNECTED") {
      statusBox.textContent = "已连接 · 消息由 Kafka 异步处理";
      sendFrame("SUBSCRIBE", { id: "chat-messages", destination: "/user/queue/chat", ack: "auto" });
      sendFrame("SUBSCRIBE", { id: "chat-ack", destination: "/user/queue/chat-ack", ack: "auto" });
      sendButton.disabled = false;
    } else if (command === "MESSAGE") {
      try {
        const data = JSON.parse(body);
        if (headers.destination && headers.destination.indexOf("chat-ack") >= 0) {
          statusBox.textContent = data.status === "QUEUED" ? "消息已进入 Kafka 队列" : "消息状态已更新";
        } else {
          renderMessage(data);
        }
      } catch (_) { /* Ignore malformed broker payloads. */ }
    } else if (command === "ERROR") {
      statusBox.textContent = "聊天连接失败，请重新登录后重试";
      sendButton.disabled = true;
    }
  }

  function loadHistory() {
    const query = isMerchant ? "?customerId=" + encodeURIComponent(selectedCustomerId()) : "";
    return axios.get("/chat/shops/" + shopId + "/messages" + query).then(function (response) {
      messageBox.innerHTML = "";
      (response.data || []).forEach(renderMessage);
      if (!messageBox.children.length) messageBox.innerHTML = '<div class="chat-empty">还没有消息，发起咨询吧。</div>';
    }).catch(function (err) {
      messageBox.innerHTML = '<div class="chat-empty">' + escapeHtml(err || "消息加载失败") + "</div>";
    });
  }

  function connect() {
    const scheme = location.protocol === "https:" ? "wss://" : "ws://";
    socket = new WebSocket(scheme + location.host + "/ws-chat");
    socket.onopen = function () {
      sendFrame("CONNECT", { "accept-version": "1.2", host: location.host, authorization: token });
    };
    socket.onmessage = function (event) {
      stompBuffer += event.data;
      const frames = stompBuffer.split("\0");
      stompBuffer = frames.pop();
      frames.forEach(processFrame);
    };
    socket.onerror = function () { statusBox.textContent = "WebSocket 连接失败"; };
    socket.onclose = function () { sendButton.disabled = true; statusBox.textContent = "连接已断开，请刷新页面重连"; };
  }

  function initialize() {
    axios.get("/user/me").then(function (response) {
      currentUserId = Number(response.data.id);
      return axios.get("/chat/merchant/shops/" + shopId + "/customers").then(function (merchantResponse) {
        isMerchant = true;
        document.getElementById("chat-title").textContent = "商家消息 · 店铺 " + shopId;
        const ids = merchantResponse.data || [];
        customerPicker.hidden = false;
        customerPicker.innerHTML = ids.map(function (id) {
          return '<option value="' + Number(id) + '">顾客 #' + Number(id) + "</option>";
        }).join("");
        customerPicker.disabled = ids.length === 0;
        if (ids.length) return loadHistory();
        messageBox.innerHTML = '<div class="chat-empty">还没有顾客发来消息。</div>';
        return null;
      }).catch(function () {
        isMerchant = false;
        document.getElementById("chat-title").textContent = "联系店铺 " + shopId;
        return loadHistory();
      });
    }).then(connect).catch(function () {
      statusBox.textContent = "登录状态失效，请重新登录";
      setTimeout(function () { location.href = "/login.html"; }, 500);
    });
  }

  customerPicker.addEventListener("change", loadHistory);
  document.getElementById("chat-form").addEventListener("submit", function (event) {
    event.preventDefault();
    const content = input.value.trim();
    if (!content || !socket || socket.readyState !== WebSocket.OPEN || !currentUserId) return;
    const payload = { shopId: shopId, content: content };
    if (isMerchant) payload.customerUserId = selectedCustomerId();
    sendFrame("SEND", { destination: "/app/chat.send", "content-type": "application/json" }, JSON.stringify(payload));
    input.value = "";
  });

  initialize();
})();
