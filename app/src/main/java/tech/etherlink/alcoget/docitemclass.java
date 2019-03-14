package tech.etherlink.alcoget;

public class docitemclass {
    public String name;
    public String date;
    public int type;
    public String client;
    public String doc_GUID;
    public int status;

    public docitemclass(String name, String date, int type, String client, String doc, int _status) {
        this.name = name;
        this.date = date;
        this.type = type;
        this.client = client;
        this.doc_GUID = doc;
        this.status = _status;
    }
}
