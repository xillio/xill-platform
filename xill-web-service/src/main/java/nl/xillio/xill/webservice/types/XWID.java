package nl.xillio.xill.webservice.types;

/**
 * XillWorker ID - unique identificator of the XillWorker in the XillWorkerPool.
 */
public class XWID {
    private final int id;

    public XWID(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }
}
