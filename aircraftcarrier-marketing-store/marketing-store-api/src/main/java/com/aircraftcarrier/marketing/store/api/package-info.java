/**
 * 对外系统提供的 API
 * 除lombok,swagger文档, 没有任何依赖
 * 入参请求全部使用实体类, 并且不要有继承关系, 包含 requestId, operator
 * 出参封装统一对象, 并且不要有继承关系, 包含 responseId, code, message, data 必选字段
 *
 * @author lzp
 */
package com.aircraftcarrier.marketing.store.api;