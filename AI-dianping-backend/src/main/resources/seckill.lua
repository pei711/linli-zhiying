--1.参数列表
--1.1优惠券id
local voucherId = ARGV[1]
--1.2用户id
local userId = ARGV[2]
--1.3订单id
local orderId = ARGV[3]

--2.数据key
--2.1库存key
local stockKey = "seckill:stock:" .. voucherId
--2.2订单key
local orderKey = "seckill:order:" .. voucherId



-- 3.脚本业务
-- 3.1.判断库存是否充足 get stockKey
local stock = tonumber(redis.call('get', stockKey))
if(stock == nil or stock <= 0) then
    -- 3.2.库存不足，返回1
    return 1
end

-- 3.2.判断用户是否下单 SISMEMBER orderKey userId
if (redis.call('sismember', orderKey,userId) == 1) then
    -- 3.3.存在，说明是重复下单，返回2
    return 2
end

-- 3.4.扣库存 incrby stockKey -1
redis.call('incrby', stockKey, -1)
-- 3.5.下单（保存用户）sadd orderKey userId
redis.call('sadd', orderKey, userId)
-- 3.6.发送消息到消息队列中， XADD stream.orders * k1 v1 k2 v2 ...
redis.call('xadd', 'stream.orders', '*', 'userId', userId, 'voucherId', voucherId, 'orderId', orderId)
-- 3.7.返回0，代表成功
return 0
