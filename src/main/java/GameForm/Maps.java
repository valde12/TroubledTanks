package GameForm;

public enum Maps {
    MAP1("C:\\Users\\ottes\\IdeaProjects\\TroubledTanks\\src\\maps\\map1.csv");

    private final String filePath;

    Maps(String filePath) {
        this.filePath = filePath;
    }

    public String getFilePath() {
        return filePath;
    }

}
