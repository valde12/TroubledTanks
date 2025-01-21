package GameForm;

public class Player {

    private Tank tank;
    private int score = 0;

    private String ip;

    public Player(TankColor Color, String ip, float startX, float startY) {
        tank = new Tank(Color, startX, startY);
        this.ip = ip;
    }

    public Tank getTank() {
        return tank;
    }
    public void setTank(Tank tank){this.tank = tank; }

    public int getScore() {
        return score;
    }

    public void winRound() {
        this.score += 1;
    }
    public String getIp(){
        return this.ip;
    }
}
