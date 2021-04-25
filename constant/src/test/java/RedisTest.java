import com.design.util.RedisUtil;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * @Description
 * @Author huangming
 * @Date 2021/4/19
 **/
public class RedisTest {

    private RedisUtil redisUtil = new RedisUtil();

    @Test
    public void testRedis(){
        HashMap<String,String> hashMap = new HashMap<>();
        hashMap.put("name","hm");
        hashMap.put("age","24");
        boolean student = redisUtil.hmset("student", hashMap);
        if (student){
            redisUtil.hmget("student", "name","age")
                    .stream().forEach(a -> System.out.println(a));
        }
    }
}
