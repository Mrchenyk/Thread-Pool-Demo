package insight;

public interface RejectHandle {

    void reject(Runnable rejectCommand,MyThreadPool myThreadPool);
}
