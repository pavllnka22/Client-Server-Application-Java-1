package package1;

public enum CommandType {
    GET_QUANTITY(1),
    DELETE_ITEMS(2),
    ADD_ITEMS(3),
    ADD_GROUP(4),
    ADD_ITEMS_TO_GROUP(5),
    SET_PRICE(6);

    private final int id;

    CommandType(int id) { this.id = id; }
    public int getId() { return id; }

    public static CommandType fromId(int id) {
        for (CommandType type : values()) {
            if (type.id == id) return type;
        }
        throw new IllegalArgumentException();
    }
}