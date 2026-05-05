package com.rad.iluminus.network;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import android.os.Handler;
import android.os.Looper;

public class DiscoveryService {

    private static final int UDP_PORT = 4210;
    private static final String BROADCAST_MESSAGE = "DISCOVER_ILUMINUS";
    private static final String EXPECTED_RESPONSE = "ILUMINUS_ACK";
    private static final int TIMEOUT_MS = 3000;

    private static final ExecutorService executor = Executors.newSingleThreadExecutor();
    private static final Handler handler = new Handler(Looper.getMainLooper());

    public interface DiscoveryCallback {
        void onDeviceFound(String ipAddress);
        void onError(String error);
    }

    public static void scanNetwork(DiscoveryCallback callback) {
        executor.execute(() -> {
            DatagramSocket socket = null;
            try {
                socket = new DatagramSocket();
                socket.setBroadcast(true);
                socket.setSoTimeout(TIMEOUT_MS);

                byte[] sendData = BROADCAST_MESSAGE.getBytes();
                
                try {
                    DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, InetAddress.getByName("255.255.255.255"), UDP_PORT);
                    socket.send(sendPacket);
                } catch (Exception e) {
                   throw new Exception("Falha no broadcast: " + e.getMessage());
                }

                byte[] recvBuf = new byte[255];
                DatagramPacket receivePacket = new DatagramPacket(recvBuf, recvBuf.length);
                
                socket.receive(receivePacket);

                String message = new String(receivePacket.getData(), 0, receivePacket.getLength()).trim();
                
                if (message.equals(EXPECTED_RESPONSE)) {
                    String ip = receivePacket.getAddress().getHostAddress();
                    handler.post(() -> callback.onDeviceFound(ip));
                } else {
                     handler.post(() -> callback.onError("Resposta inválida recebida"));
                }

            } catch (Exception e) {
                handler.post(() -> callback.onError("Erro ao escanear a rede"));
            } finally {
                if (socket != null && !socket.isClosed()) {
                    socket.close();
                }
            }
        });
    }
}
