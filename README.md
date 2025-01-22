This game is Trouble Tanks

To run this game there's a

There's a method en Server.Java called Server() it is important that it is run
on a separate computer
and that you know the IPv4 Address of that computer that runs it

```Java
public void Server () {

        SpaceRepository repository = new SpaceRepository();

        SequentialSpace gameRooms = new SequentialSpace();

        repository.add("gameRooms",gameRooms);

        repository.addGate( TCP_PREFIX + HOST_IP + ":" + HOST_PORT + "/?keep");
        System.out.println("Server started and waiting for connections...");

        synchronized (this) {
            try {
                this.wait();
            } catch (InterruptedException e) {
                System.err.println("Server interrupted: " + e.getMessage());
            }
        }
        System.out.println("Server stopping...");
}
```


In both Server.java and Client.java there's private static final String SERVER_IP (will be marked with TODO)
those values must be changed to the IPv4 Address of the computer that runs the Server() method


In Maps.java we use the file path of the map1.csv file, so you must find the map1.csv's file path
and change it in the Maps.java (marked with TODO)

