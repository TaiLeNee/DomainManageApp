package model;

public enum Role {
    ADMIN(1, "ADMIN"),
    BUYER(2, "BUYER");

    private final int id;
    private final String name;

    Role(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    // Phương thức hữu ích để tìm Role theo id
    public static Role getById(int id) {
        for (Role role : values()) {
            if (role.id == id) {
                return role;
            }
        }
        throw new IllegalArgumentException("Không tìm thấy Role với id: " + id);
    }

    // Phương thức hữu ích để tìm Role theo name
    public static Role getByName(String name) {
        for (Role role : values()) {
            if (role.name.equals(name)) {
                return role;
            }
        }
        throw new IllegalArgumentException("Không tìm thấy Role với name: " + name);
    }
}