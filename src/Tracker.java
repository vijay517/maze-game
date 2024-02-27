package src;

import java.rmi.RemoteException;
import java.rmi.registry.Registry;
import java.rmi.registry.LocateRegistry;

public class Tracker {

    private RemoteTrackerObject remoteTrackerObject;

    public Tracker(int N, int K) throws RemoteException {
        this.remoteTrackerObject = new RemoteTrackerObject(N, K);
    }

    public static void main(String args[]) {
        if (args.length != 3) {
            System.err.println("Usage: java Tracker [port-number] [N] [K]");
            System.exit(1);
        }

        try {
            int portNumber = Integer.parseInt(args[0]);
            int N = Integer.parseInt(args[1]);
            int K = Integer.parseInt(args[2]);

            Tracker tracker = new Tracker(N, K);
            RemoteTrackerInterface stub = (RemoteTrackerInterface) tracker.remoteTrackerObject;

            // Bind the remote object's stub in the registry
            Registry registry = LocateRegistry.createRegistry(portNumber);
            registry.rebind("remoteTrackerObject", stub);

            System.err.println("Server ready");
        } catch (Exception e) {
            System.err.println("Server exception: " + e.toString());
            e.printStackTrace();
        }
    }
}