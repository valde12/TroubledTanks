public class Player {

    private Tank tank;

    public Player(TankColor Color) {
        tank = new Tank(Color);
    }

    public Tank getTank() {
        return tank;
    }
}
