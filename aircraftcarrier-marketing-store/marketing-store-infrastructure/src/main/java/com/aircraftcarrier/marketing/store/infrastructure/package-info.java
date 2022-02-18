/**
 * 基础设施层。
 * 实现Gateway接口, 外部数据源转换为本地实体
 * 提供Gateway接口, 查询do层, 结果交给视图层, app层
 * <p>
 * config:      配置相关
 * convert:     转换
 * event:       事件
 * gatewayImpl: 把外部 Es, Redis, DB等实体, 封装成本系统entity,或领域对象, 提供给domain层, 或者app层
 * repository:  DB层
 * es:          检索
 * rpc:         调用外部接口
 *
 * @author lzp
 */
package com.aircraftcarrier.marketing.store.infrastructure;