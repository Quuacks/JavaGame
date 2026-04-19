package io.github.some_example_name;

public class TileData {
    public int posX;
    public int posY;
    public boolean hasCollision;
    public int tileID;

    public TileData() {}

    public TileData(int posX, int posY, boolean hasCollision, int tileID){
        this.posX = posX;
        this.posY = posY;
        this.hasCollision = hasCollision;
        this.tileID = tileID;
    }
}
