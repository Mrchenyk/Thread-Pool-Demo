package insight;


import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

//什么是线程池？
//创建线程后，我们不希望销毁线程，我们希望这个线程能够复用，并且我们要管理这些线程
public class MyThreadPool {

    //我们的线程池应该有多少线程？
    private final int corePoolSize;
    private final int maxSize;
    private final int timeout;
    private final TimeUnit timeUnit;
    private final BlockingQueue<Runnable>commandList;
    private final RejectHandle rejectHandle;

    public MyThreadPool(int corePoolSize, int maxSize, int timeout, TimeUnit timeUnit, ArrayBlockingQueue<Runnable> commandList,RejectHandle rejectHandle) {
        this.corePoolSize = corePoolSize;
        this.maxSize = maxSize;
        this.timeout = timeout;
        this.timeUnit = timeUnit;
        this.commandList=commandList;
        this.rejectHandle=rejectHandle;
    }

    //1.线程什么时候创建
    //2. 线程的runnable是什么？是我们提交的command吗？

//    //线程执行完任务后就会销毁，所以我们把任务放进List里，让线程不断去读取List里的任务，以实现线程复用
//    List<Runnable>commandList = new ArrayList<Runnable>();
//
//    Thread thread=new Thread(()->{
//        while(true){
//            if(!commandList.isEmpty()){
//                Runnable command = commandList.get(0);
//                command.run();
//                commandList.remove(0);
//            }
//        }
//    });

    //使用BlockingQueue解决while循环消耗CPU资源的问题
    //放进构造函数让用户决定阻塞队列
//    BlockingDeque<Runnable>commandList=new LinkedBlockingDeque<Runnable>(1024);


//    Thread thread = new Thread(()->{
//       while(true){
//           try {
//               Runnable command = commandList.take();
//               command.run();
//           } catch (InterruptedException e) {
//               throw new RuntimeException(e);
//           }
//       }
//    },"唯一线程");
//
//    {
//        thread.start();
//    }

//    Runnable coreTask =()->{
//        while(true){
//            try {
//                Runnable command = commandList.take();
//                command.run();
//            } catch (InterruptedException e) {
//                throw new RuntimeException(e);
//            }
//        }
//    };

//    Runnable supportTask =()->{
//        while(true){
//            try {
//                //超过5秒获取不到任务则辅助线程销毁
//                Runnable command = commandList.poll(5, TimeUnit.SECONDS);
//                if(command==null){
//                    break;
//                }
//                command.run();
//            } catch (InterruptedException e) {
//                throw new RuntimeException(e);
//            }
//        }
//        System.out.println("辅助线程:"+Thread.currentThread().getName()+"结束");
//    };



//    List<Thread>threadList=new ArrayList<>();

    //核心线程
    List<Thread>coreList=new ArrayList<>();
    //辅助线程
    List<Thread>supportList=new ArrayList<>();



    void execute(Runnable r) {
        //判断thread list中有多少个元素，如果没达到corePoolSize,就应该创建线程
        if(coreList.size()<corePoolSize){
            Thread thread = new CoreThread();
            //创建好的线程放进threadList管理
            coreList.add(thread);
            //启动线程
            thread.start();
        }

        //如果offer的返回值是false，则说明阻塞队列已经满了，我们可以在corePoolSize的基础上再增加几个线程，来帮我们处理新的任务
        if(!commandList.offer(r)){
            //如果没达到maxSize就可以继续创建辅助线程
            if(coreList.size()+supportList.size()<maxSize){
                Thread thread = new SupportThread();
                supportList.add(thread);
                thread.start();
            }
        }

        //拒绝策略
        if (commandList.offer(r)) {
//            throw new RuntimeException("阻塞队列满了");
            rejectHandle.reject(r,this);
        }
    }

    class CoreThread extends Thread{
        @Override
        public void run() {
            while(true){
                try {
                    Runnable command = commandList.take();
                    command.run();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    class SupportThread extends Thread{
        @Override
        public void run() {
            while(true){
                try {
                    //超过5秒获取不到任务则辅助线程销毁
                    Runnable command = commandList.poll(timeout, timeUnit);
                    if(command==null){
                        break;
                    }
                    command.run();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
            System.out.println("辅助线程:"+Thread.currentThread().getName()+"结束");
        }
    }
}
