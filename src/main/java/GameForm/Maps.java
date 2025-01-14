package GameForm;

public enum Maps {
    MAP1("C:\\Users\\caspe\\IdeaProjects\\TroubledTanks\\src\\maps\\map1.csv");

    private final String filePath;

    Maps(String filePath) {
        this.filePath = filePath;
    }

    public String getFilePath() {
        return filePath;
    }

}
