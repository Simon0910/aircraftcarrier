import com.aircraftcarrier.framework.core.AbstractLogger;
import com.aircraftcarrier.framework.tookit.Log;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;

/**
 * 类注释内容
 *
 * @author zhipengliu
 * @date 2023/9/3
 * @since 1.0
 */
@Slf4j
public class LogController extends AbstractLogger {
    static Object nullObject = null;
    static HashMap<Object, Object> emptyObject = new HashMap<>();
    static HashMap<Object, Object> orderInfo = new HashMap<>();

    static {
        orderInfo.put("id", "1");
        orderInfo.put("orderNo", "123");
        orderInfo.put("orderInfoDetail", emptyObject);
        orderInfo.put("isNull", nullObject);
    }

    public static void main(String[] args) {

        Log.info("aaa {}, {}", getSupplier(1), getToJsonSupplier(orderInfo));

        Log.info(log,"bbb {}, {}", getSupplier(2), getToJsonSupplier(orderInfo));

    }
}
