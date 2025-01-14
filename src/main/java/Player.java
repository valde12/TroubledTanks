public class Player {

    private Tank tank;
    private int score = 0;

    public Player(TankColor Color, float startX, float startY) {
        tank = new Tank(Color, startX, startY);
    }

    public Tank getTank() {
        return tank;
    }

    public int getScore() {
        return score;
    }

    public void winRound() {
        this.score += 1;
    }
}
