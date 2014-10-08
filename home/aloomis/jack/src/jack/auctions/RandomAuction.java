package jack.auctions;

import java.util.Map;
import java.util.Random;
import java.util.Vector;
import java.util.HashMap;

import jack.server.ComThread;

public class RandomAuction extends AuctionBase
{
    Random random = new Random();
    private long endTime = 0;
    private long restartTime = 0;

    public RandomAuction(int auctionId) {
        super(auctionId);
    }

    @Override
    protected void initialize() {
        endTime = System.currentTimeMillis() + random.nextInt(10000);
        sendMessage("start");
    }

    @Override
    protected void idle() {
        if (endTime != 0 && System.currentTimeMillis() > endTime) {
            if (tryEndable()) {
                endTime = 0;
                //restartTime = System.currentTimeMillis() + random.nextInt(10000);
            }

        } else if (restartTime != 0 && System.currentTimeMillis() > restartTime) {
            if (tryResume()) {
                endTime = System.currentTimeMillis() + random.nextInt(10000);
                restartTime = 0;
            }
        }
    }

    @Override
    protected void resolve() {
        sendMessage("stop");
    }

    protected void sendMessage(String type) {
        sendMessage(type, new HashMap<String, String>());
    }
}
