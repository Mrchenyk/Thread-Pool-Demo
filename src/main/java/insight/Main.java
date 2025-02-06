package insight;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;

public class Main {
    public static void main(String[] args) {
        MyThreadPool myThreadPool=new MyThreadPool(2,4,1, TimeUnit.SECONDS,new ArrayBlockingQueue<>(2),new ThrowRejectHandle());
        for(int i=0;i<10;i++){
            myThreadPool.execute(()->{
                //模拟延迟
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                //执行打印任务
                System.out.println(Thread.currentThread().getName());
            });
        }
    }
}
