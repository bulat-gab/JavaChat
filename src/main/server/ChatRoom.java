package main.server;

public class ChatRoom {
    private int _id;
    private String _name;

    public ChatRoom(int _No, String _name) {
        this._id = _No;
        this._name = _name;
    }

    public int getId() {
        return _id;
    }

    public String getName() {
        return _name;
    }

    @Override
    public String toString() {
        return _id + ":" + _name;
    }
}
