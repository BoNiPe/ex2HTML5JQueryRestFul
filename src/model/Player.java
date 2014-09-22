package model;

public class Player {
    int id;
    String name;
    String country;

    public Player(int id, String name, String country)
    {
        this.id = id;
        this.name = name;
        this.country = country;
    }

    public int getId()
    {
        return id;
    }

    public String getName()
    {
        return name;
    }

    public String getCountry()
    {
        return country;
    }
    
}
