package jack.auctions;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import jack.server.ComThread;
import jack.server.ClientHandler;
import jack.scheduler.Scheduler;

public abstract class AuctionBase implements Runnable
{
    /** A new auction that has not started is in this state */
    public static final int STATE_NEW = 1;

    /** An auction that is running is in this state */
    public static final int STATE_RUNNING = 2;

    /** An auction that is running and can be ended is in this state */
    public static final int STATE_ENDABLE = 3;

    /** An auction that is in the process of ending is in this state */
    public static final int STATE_ENDING = 4;

    /** An auction that is ended is in this state */
    public static final int STATE_ENDED = 5;

    /** The id of the session this auction is part of */
    protected int sessionId = 0;

    /** The unique id of this auction within the session */
    protected final int auctionId;

    /** The parameters used to initialize this auction */
    protected Map<String, String> params = new HashMap<String, String>();

    /** A collection of sockets for client communication */
    protected Vector<ClientHandler> clients;


    /** The minium time to wait before idle function calls in ms */
    private static final long IDLE_TIMEOUT = 50;

    /** The current state of the auction */
    private int state;

    /** The lock that the state is synchronized on */
    private Object stateLock = new Object();

    /** A queue of messages to handle */
    private final BlockingQueue<String> messages;

    /** A map of message handlers */
    private final Map<String, MessageHandler> handlers;

    /** Logger for writing log messages */
    private final Logger LOGGER = Logger.getLogger(AuctionBase.class.getName());

    /**
     * Constructs an auction with the specified identification.
     * @param auctionId The unique identified of this auction
     */
    public AuctionBase(int auctionId) {
        this.auctionId = auctionId;

        state = STATE_NEW;
        messages = new LinkedBlockingQueue<String>();
        handlers = new HashMap<String, MessageHandler>();
    }

    /**
     * Sets the session identifier that this auction is a part of.
     * @param newSessionId The new session that this auction is a part of
     */
    public final void setSessionId(int newSessionId) {
        sessionId = newSessionId;
    }

    /**
     * This function returns the unique identifier of this auction. Each auction
     * is assigned an integer identifier suring constructor. This value can be
     * used to uniquely identify the auction within a schedule or in a log file.
     * @return The integer identifier of this auction
     */
    public final int getId() {
        return auctionId;
    }

    /**
     * Sets the client handlers to use for all auction communication. When the
     * auction is started, it will register itself with these clients, and when
     * the auction is ended, it will unregister itself with these clients.
     * @param newClients The new set of client handlers
     */
    public final void setClients(Vector<ClientHandler> newClients) {
        clients = newClients;
    }

    /**
     * This function sets the lock object used to protect the current state of
     * the auction. Each auction is initially constructed with its own lock,
     * which allows the state to be queried and set in a multithreaded
     * environment. This function is useful when multiple auctions need to be
     * synchronized on a single lock object, such as when they are managed by a
     * schedule.
     * @param newStateLock The new lock to synchronize the auction state on
     */
    public final void setStateLock(Object newStateLock) {
        stateLock = newStateLock;
    }

    /**
     * This function returns the current state of this auction. The state is one
     * of STATE_NEW, STATE_RUNNING, STATE_ENDABLE, STATE_ENDING, or STATE_ENDED.
     * This function is thread safe.
     * @return The current state of this auction
     */
    public final int getState() {
        synchronized (stateLock) {
            return state;
        }
    }

    /**
     * This function tries to end this auction. An auction can end if it is in
     * the STATE_ENDABLE state. If this function succeeds it returns true and
     * moves the auction from STATE_ENDABLE into STATE_ENDING, otherwise it
     * returns false. This function should be used by a scheduler to end
     * auctions which may be synchronized with other auctions. This function is
     * thread safe.
     * @return True on success and false otherwise
     */
    public final boolean tryEnd() {
        return setState(STATE_ENDING);
    }

    /**
     * This function tries to resume an auction. An auction can be resumed if it
     * is in the STATE_ENDABLE state. If this function succees it returns true
     * and moves the auction from STATE_ENDABLE into STATE_RUNNING, otherwise it
     * returns false. This function should be used by an auction subclass that
     * for whatever reason should no longer be ended. This function is thread
     * safe.
     * @return True on success and false on failure
     */
    public final boolean tryResume() {
        return setState(STATE_RUNNING);
    }

    /**
     * This function blocks until the current auction moves into STATE_ENDED. It
     * should be used with care as this may take an undefined amount of time. In
     * general it should be called after a call to tryEnd has returned true.
     * This function is thread safe.
     */
    public final void waitForEnd() {
        synchronized (stateLock) {
            try {
                while (state != STATE_ENDED) {
                    stateLock.wait();
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    /**
     * Adds the message to the end of the message queue. This function should be
     * called by the ComThread whenever a new message arrives for this auction.
     * The message queue is drained and each message is passed to its
     * corresponding handler when the auction is in STATE_RUNNING or
     * STATE_ENDABLE. This function is thread safe, but it is not synchronized
     * on the same lock as the auctions state.
     * @param message Message to be handled by this auction
     */
    public void queueMessage(String message) {
        try {
            messages.put(message);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    /**
     * This function sets the specified auction parameters. Subclasses should
     * override this function to receive configuration parameters. These
     * parameters are read from the configuration xml file and passed to each
     * auction by the AuctionFactory.
     * @param newParams A map of configuration parameters.
     */
    public void setParams(Map<String, String> newParams) {
        params = new HashMap<String, String>(newParams);
    }

    /**
     * This function sends the auction specification to each of the clients. The
     * auction specification consists of the "auction" tag followed by all of
     * the parameters used to initialize the auction. Subclasses should override
     * this function to only send the information that they want.
     */
    public void sendSpec() {
        sendMessage("auction", params);
    }

    /**
     * This function runs the auction. All auctions at their most basic level
     * can be represented as a series of messages passed between the auctions
     * and thir bidders. This function tries to take care of most of the leg
     * work of receiving messages. It drains the message queue and passes each
     * message to its registered handler. When their are no messages to drain it
     * calls the idle function in which a subclass can do whatever they would
     * like.
     */
    @Override
    public void run() {

        // If this auction has already been run then it cannot be run again.
        // This restriction could be relaxed in the future, but for the moment
        // it seems like the safest behavior.

        if (getState() != STATE_NEW) {
            return;
        }

        // Initialize the auction. This involes setting the state to be
        // STATE_RUNNING, registering the auction with the ComThreads so that
        // they can receive messages from the bidders, and calling the auction
        // specific initialization routine.

        setState(STATE_RUNNING);
        register();
        initialize();

        try {

            // Begin processing messages. As long as the curretn state of the
            // auction is not state ending we will continue to process messages.
            // This includes STATE_ENDABLE, where an auction can still receive
            // messages, but may be ended at any time.

            while (getState() < STATE_ENDING) {

                // Get the message off the top of the queue. If there is no
                // message then idle and try again.

                String message = messages.poll(IDLE_TIMEOUT,
                                               TimeUnit.MILLISECONDS);
                if (message == null) {
                    idle();
                    continue;
                }

                // Split the message by whitespace. Here we expect an auction
                // message starts with the type and is followed by an
                // unspecified number of key=value pairs:
                // "messageType key1=value1 ... keyN=valueN"
                // Messages that do not fit this format will either not be
                // processed or processed incorrectly.

                String[] keyVals = message.split("\\s+");
                if (keyVals.length > 0) {

                    // Parse the message type and the arguments

                    String messageType = keyVals[0];
                    Map<String, String> args =
                        toMap(Arrays.copyOfRange(keyVals, 1, keyVals.length));

                    // Pass the message to the appropriate handler and
                    // ignore any unknown messages.

                    MessageHandler handler = handlers.get(messageType);
                    if (handler != null) {
                        try {
                            handler.handle(args);
                        } catch (IllegalArgumentException e) {
                            LOGGER.warning(e.toString());
                        }
                    } else {
                        LOGGER.warning("Unknown message type: " + messageType);
                    }
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // Finally resolve the message, unregister this auction from the
        // ComThreads and set the state to be ended.

        resolve();
        unregister();
        setState(STATE_ENDED);
    }

    /**
     * Called Immediately before the auction begins. Subclasses should override
     * this method to setup their own initialization routines and send any
     * pertinant information to the bidders.
     */
    protected void initialize() {}

    /**
     * Called whenever there are no messages to process. Subclasses should
     * override this method if they want to perform actions that are not
     * triggered by client messages, such as timed events.
     */
    protected void idle() throws InterruptedException {}

    /**
     * Called immediately after the auction ends. Subclasses should override
     * this method to setup their own resolution routines. This generally
     * includes informing the bidders of the winner.
     */
    protected void resolve() {}

    /**
     * Adds the specified handler to the handler map. If a handler has already
     * been specified for the given message type then this call will replace
     * that handler.
     * @param type The message type that this handler should be called on.
     * @param handler The handler responsible for handling this message type.
     */
    protected final void putHandler(String type, MessageHandler handler) {
        handlers.put(type, handler);
    }

    /**
     * Sends a message of the specifed type with the specifeid parameters to
     * each of the clients. In addition to the arguments passed into this
     * function, the sessionId and auctionId will be automatically appended.
     * @param type The message type that is being sent
     * @param args A key value map of arguments to pass with that message
     */
    protected final void sendMessage(String type, Map<String, String> args) {
        args.put("sessionId", Integer.toString(sessionId));
        args.put("auctionId", Integer.toString(auctionId));

        String message = type + toString(args);
        for (ClientHandler client : clients) {
            client.sendMessage(message);
        }
    }

    /**
     * This function trys to set the current auction state to STATE_ENDABLE. It
     * should be called by subclasses when they are able to be ended. It returns
     * true if the auction is currently in STATE_RUNNING and false otherwise.
     * @return True on success and false on failure
     */
    protected final boolean tryEndable() {
        return setState(STATE_ENDABLE);
    }

    /**
     * This function attempts to transition the auction to the specified state.
     * This function represents the core auction state machine. If the auction
     * can transition to the specified state this function will return true
     * otherwise it will return false. This function is thread safe.
     * @param newState The state to try and transitions into
     * @return True on success and false on failure
     */
    private final boolean setState(int newState) {
        synchronized (stateLock) {
            switch (state) {

                // Auctions in STATE_NEW can only transition to STATE_RUNNING.
                // This happens the first time that the auctions run method is
                // called. Auctions in STATE_RUNNING can send and receive
                // message from their clients.

                case STATE_NEW:
                    if (newState != STATE_RUNNING) {
                        return false;
                    }
                    LOGGER.fine(String.format("Auction %d running\n", auctionId));
                    state = STATE_RUNNING;
                    stateLock.notify();
                    return true;

                // Auctions in STATE_RUNNING can only transition to
                // STATE_ENDABLE. This happens when an auction subclass
                // determines that it can be ended. Auction in STATE_ENDABLE can
                // still send and receive message from their clients.

                case STATE_RUNNING:
                    if (newState != STATE_ENDABLE) {
                        return false;
                    }
                    LOGGER.fine(String.format("Auction %d endable\n", auctionId));
                    state = STATE_ENDABLE;
                    stateLock.notify();
                    return true;

                // Auctions in STATE_ENDABLE can transition either to
                // STATE_RUNNING or STATE_ENDING. Transitioning back to
                // STATE_RUNNING can be done by a subclass by calling tryResume,
                // and transitioning to STATE_ENDING can be done by a schedular
                // by calling tryEnd.

                case STATE_ENDABLE:
                    if (newState == STATE_RUNNING) {
                        LOGGER.fine(String.format("Auction %d running\n", auctionId));
                        state = STATE_RUNNING;
                        stateLock.notify();
                        return true;
                    }
                    if (newState == STATE_ENDING) {
                        LOGGER.fine(String.format("Auction %d ending\n", auctionId));
                        state = STATE_ENDING;
                        stateLock.notify();
                        return true;
                    }
                    return false;

                // Auctions in STATE_ENDING can no longer send and receive
                // messages from their clients, but they have no been officially
                // resolved. From here the only state that can be transitioned
                // to is STATE_ENDED.

                case STATE_ENDING:
                    if (newState != STATE_ENDED) {
                        return false;
                    }
                    LOGGER.fine(String.format("Auction %d ended\n", auctionId));
                    state = STATE_ENDED;
                    stateLock.notify();
                    return true;

                // STATE_ENDED is a terminal state, and once here an auction is
                // essentially useless, except to query for information about
                // the bids placed while it was running.

                default:
                case STATE_ENDED:
                    return false;
            }
        }
    }

    /**
     * This function registers this auction with all of its clients. Once
     * registered this auction will receive message from these clients, which
     * will be palced in the message queue and passed to their corresponding
     * handlers.
     */
    private final void register() {
        for (ClientHandler client : clients) {
            client.register(this);
        }
    }

    /**
     * This function unregisters this auction with all of its clients. After
     * unregistering this auction no no longer be able to receive messages from
     * its clients.
     */
    private final void unregister() {
        for (ClientHandler client : clients) {
            client.unregister(this);
        }
    }

    /**
     * Constructs a map from an array of key value strings. Each string in the
     * array should be of the form "key=value". Extra whitespace on either end
     * of the key or value will be trimmed. Any non conformant strings will be
     * silently ignored.
     * @param keyVals An array of strings of the form "key=value"
     * @return A map of values indexed by their corresponding keys
     */
    private static final Map<String, String> toMap(String[] keyVals) {
        Map<String, String> m = new HashMap<String, String>();
        for (String keyVal : keyVals) {
            String[] pair = keyVal.split("=");
            if (pair.length == 2) {
                m.put(pair[0].trim(), pair[1].trim());
            }
        }
        return m;
    }

    /**
     * Constructs a key value string from a map of strings. Each key value pair
     * in the result will be of the form "key=value" seperated from ech other by
     * whitespace.
     * @param map A map of key value pairs
     * @return A string of key value pairs
     */
    public static final String toString(Map<String, String> keyVals) {
        String s = new String();
        for (Map.Entry<String, String> entry : keyVals.entrySet()) {
            s += " " + entry.getKey() + "=" + entry.getValue().replace(' ', '_');
        }
        return s;
    }

    /**
     * New method to exchange the budget
     * @return HashMap with budgets
     */
    public Map<String, Integer> getBudgets(){
	return null;
    }

    /**
     * New method  to set the budgets
     */
    public void setBudgets(Map<String, Integer> newBudgets){
    }


    /**
     * New method to update budgets
     */
    public void updateBudgets(){
    }    

    protected interface MessageHandler {
        public void handle(Map<String, String> args) throws IllegalArgumentException;
    }

    /**
     * New method to track roster
     */
    public void updateRosters(){
    }  

    /**
     * New method  to set the rosters
     */
    public void setRosters(Map<String, LinkedList<String>> newRoster){
    }

    /**
     * New method to exchange the roster
     * @return HashMap with budgets
     */
    public Map<String, LinkedList<String>> getRosters(){
	return null;
    }
}
