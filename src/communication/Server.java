package communication;

import java.io.*;

public class Server {
    public static void main(String[] args) throws IOException {
        new ServerThread().start();
    }
}