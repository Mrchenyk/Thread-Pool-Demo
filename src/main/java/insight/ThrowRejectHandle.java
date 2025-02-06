package insight;

import java.util.concurrent.RejectedExecutionException;

public class ThrowRejectHandle implements RejectHandle{
    @Override
    public void reject(Runnable rejectCommand, MyThreadPool myThreadPool) {
        throw new RuntimeException("阻塞队列满了");
    }
}
