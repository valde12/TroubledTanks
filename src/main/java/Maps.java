public enum Maps {
    MAP1("src/maps/map1.csv");

    private final String filePath;

    Maps(String filePath) {
        this.filePath = filePath;
    }

    public String getFilePath() {
        return filePath;
    }

}
