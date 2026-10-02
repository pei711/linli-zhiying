(function () {
  var MAX_ATTEMPTS = 10;
  var RETRY_DELAY = 200;
  var attemptCount = 0;

  function mountAssistantPage() {
    var container = document.getElementById("ai-page");
    if (!container) {
      return;
    }

    if (!window.Vue) {
      attemptCount += 1;
      if (attemptCount <= MAX_ATTEMPTS) {
        setTimeout(mountAssistantPage, RETRY_DELAY);
      }
      return;
    }

    new Vue({
      el: "#ai-page",
      template: `
        <div class="ai-page-card">
          <header class="ai-chat-header">
            <div class="ai-chat-header-left">
              <div class="ai-brand">
                <i class="el-icon-service"></i>
              </div>
              <div class="ai-header-title">
                <span>AI探店助手</span>
                <span>AI探店助手陪您一起解锁探店新思路</span>
              </div>
            </div>
            <div class="ai-chat-header-actions">
              <button
                class="ai-icon-button"
                @click="startNewConversation"
                :disabled="isLoading"
                title="开启新对话"
                aria-label="开启新对话"
              >
                <i class="el-icon-plus"></i>
              </button>
              <button
                class="ai-close-button"
                @click="handleClose"
                title="返回上一页"
                aria-label="关闭并返回"
              >
                <i class="el-icon-close"></i>
              </button>
            </div>
          </header>

          <main class="ai-chat-body" ref="chatContainer">
            <div
              v-for="(message, index) in messages"
              :key="index"
              class="ai-message-row"
              :class="{ 'ai-right': message.role === 'user' }"
            >
              <div class="ai-avatar">
                <i :class="message.role === 'user' ? 'el-icon-user' : 'el-icon-service'"></i>
              </div>
              <div class="ai-message-bubble">
                <div
                  v-if="message.role === 'assistant' && message.isLoading"
                  class="ai-typing-dots"
                >
                  <span></span><span></span><span></span>
                </div>
                <div v-else>{{ message.content }}</div>
                <span v-if="message.isStreaming" style="margin-left: 2px;">|</span>
              </div>
            </div>
          </main>

          <footer class="ai-chat-footer">
            <textarea
              ref="textarea"
              v-model="userInput"
              class="ai-input"
              rows="1"
              placeholder="请问我有什么可以帮助您的嘛..."
              @keydown.enter.exact.prevent="sendMessage"
              @input="adjustTextareaHeight"
            ></textarea>
            <button
              class="ai-send-button"
              @click="isLoading ? stopResponse() : sendMessage()"
              :disabled="!isLoading && !userInput.trim()"
              :title="isLoading ? '停止回答' : '发送'"
              aria-label="发送消息"
            >
              <i :class="isLoading ? 'el-icon-switch-button' : 'el-icon-s-promotion'"></i>
            </button>
          </footer>
        </div>
      `,
      data: function () {
        return {
          messages: [],
          userInput: "",
          isLoading: false,
          sessionId: Date.now().toString(),
          controller: null,
        };
      },
      mounted: function () {
        this.startNewConversation();
        window.addEventListener("beforeunload", this.stopResponse);
        this.$nextTick(this.focusTextarea);
      },
      beforeDestroy: function () {
        window.removeEventListener("beforeunload", this.stopResponse);
      },
      watch: {
        messages: {
          handler: function () {
            this.scrollToBottom();
          },
          deep: true,
        },
      },
      methods: {
        handleClose: function () {
          if (this.isLoading) {
            this.stopResponse();
          }
          if (window.history.length > 1) {
            window.history.back();
          } else if (document.referrer) {
            window.location.href = document.referrer;
          } else {
            window.location.href = "./index.html";
          }
        },
        startNewConversation: function () {
          this.stopResponse();
          this.messages = [];
          this.sessionId = Date.now().toString();
          this.messages.push({
            role: "assistant",
            content: "你好! 我是您的专属AI探店助手,请问有什么能帮到您？",
            isLoading: false,
            isStreaming: false,
          });
          this.$nextTick(this.scrollToBottom);
          this.$nextTick(this.focusTextarea);
        },
        adjustTextareaHeight: function () {
          var textarea = this.$refs.textarea;
          if (!textarea) {
            return;
          }
          textarea.style.height = "auto";
          textarea.style.height = Math.min(textarea.scrollHeight, 200) + "px";
        },
        scrollToBottom: function () {
          var _this = this;
          this.$nextTick(function () {
            var container = _this.$refs.chatContainer;
            if (container) {
              container.scrollTop = container.scrollHeight;
            }
          });
        },
        focusTextarea: function () {
          var _this = this;
          this.$nextTick(function () {
            var textarea = _this.$refs.textarea;
            if (textarea) {
              textarea.focus();
              _this.adjustTextareaHeight();
            }
          });
        },
        sendMessage: function () {
          return this._sendMessage();
        },
        _sendMessage: async function () {
          if (!this.userInput.trim() || this.isLoading) {
            return;
          }

          if (this.controller) {
            this.controller.abort();
          }

          if (typeof AbortController !== "undefined") {
            this.controller = new AbortController();
          } else {
            this.controller = null;
          }

          var userMessage = {
            role: "user",
            content: this.userInput.trim(),
            isLoading: false,
            isStreaming: false,
          };
          this.messages.push(userMessage);

          var assistantMessage = {
            role: "assistant",
            content: "",
            isLoading: true,
            isStreaming: false,
          };
          this.messages.push(assistantMessage);

          var question = this.userInput.trim();
          this.userInput = "";
          this.adjustTextareaHeight();
          this.scrollToBottom();
          this.isLoading = true;

          try {
            var options = {
              method: "GET",
              headers: {},
            };

            // 添加 token 到请求头
            var token = sessionStorage.getItem("token");
            if (token) {
              options.headers["authorization"] = token;
            }

            if (this.controller && this.controller.signal) {
              options.signal = this.controller.signal;
            }

            var response = await fetch(
              "/api/chat?message=" +
                encodeURIComponent(question) +
                "&memoryId=" +
                this.sessionId,
              options
            );
            if (!response.ok) {
              throw new Error("HTTP error! status: " + response.status);
            }

            var messageIndex = this.messages.length - 1;
            var reader =
              response.body && response.body.getReader
                ? response.body.getReader()
                : null;

            if (!reader) {
              var text = await response.text();
              this.messages[messageIndex].content = text;
            } else {
              var decoder = new TextDecoder("utf-8");
              var buffer = "";
              this.messages[messageIndex].isLoading = false;
              this.messages[messageIndex].isStreaming = true;

              while (true) {
                var _ref = await reader.read();
                var done = _ref.done;
                var value = _ref.value;
                if (done) {
                  this.messages[messageIndex].isStreaming = false;
                  break;
                }
                buffer += decoder.decode(value, { stream: true });
                this.messages[messageIndex].content = buffer;
                this.scrollToBottom();
              }
            }
          } catch (error) {
            if (error.name !== "AbortError") {
              console.error("请求出错:", error);
              var lastMessage = this.messages[this.messages.length - 1];
              if (lastMessage) {
                lastMessage.content =
                  "抱歉，请求过程中出现错误: " + error.message;
                lastMessage.isLoading = false;
                lastMessage.isStreaming = false;
              }
            }
          } finally {
            this.isLoading = false;
            if (this.messages.length) {
              var finalMessage = this.messages[this.messages.length - 1];
              finalMessage.isLoading = false;
              finalMessage.isStreaming = false;
            }
            this.controller = null;
            this.scrollToBottom();
          }
        },
        stopResponse: function () {
          if (this.controller) {
            this.controller.abort();
          }
          this.controller = null;
          this.isLoading = false;
          if (this.messages.length) {
            var lastMessage = this.messages[this.messages.length - 1];
            lastMessage.isLoading = false;
            lastMessage.isStreaming = false;
          }
        },
      },
    });
  }

  function scheduleMount() {
    if (document.readyState === "loading") {
      document.addEventListener("DOMContentLoaded", mountAssistantPage, {
        once: true,
      });
    } else {
      setTimeout(mountAssistantPage, 0);
    }
  }

  scheduleMount();
})();
