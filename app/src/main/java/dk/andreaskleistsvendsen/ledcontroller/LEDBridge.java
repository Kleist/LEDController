package dk.andreaskleistsvendsen.ledcontroller;

import android.os.AsyncTask;
import android.util.Log;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.lang.Thread;

public class LEDBridge {
    private final InetAddress ip_;
    private final int port_;
    public enum LEDState {
        ALL_ON,
        ALL_OFF,
        UNKNOWN_ERROR,
        DONE
    }

    private class SendPacketTask extends AsyncTask<Byte, Void, LEDState> {
        private LEDBridge parent_;
        public SendPacketTask(LEDBridge parent) {
            parent_ = parent;
        }

        @Override
        protected LEDState doInBackground(Byte... ds) {
            DatagramSocket socket = null;
            byte[] data = new byte[ds.length];
            for (int i=0; i<ds.length; ++i) {
                data[i] = ds[i];
            }
            try {
                socket = new DatagramSocket();
                Log.d("Packet", String.format("Preparing packet: %d, %d, %d", data[0], data[1], data[2]));
                DatagramPacket packet = new DatagramPacket(data, data.length, ip_, port_);
                Thread.sleep(1000);
                Log.d("Packet", "Sending packet");
                socket.send(packet);
                Thread.sleep(1000);
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
                return LEDState.UNKNOWN_ERROR;
            } catch (InterruptedException e) {
                e.printStackTrace();
                return LEDState.UNKNOWN_ERROR;
            } finally {
                if (socket != null) {
                    socket.close();
                }
            }
            return LEDState.DONE;
        }
    };

    public LEDBridge(String ip, int port) throws UnknownHostException {
        ip_ = InetAddress.getByName(ip);
        port_ = port;
    }

    public void rgbOn() throws IOException {
        sendCommand_(0x42);
    }

    public void rgbOff() throws IOException {
        sendCommand_(0x41);
    }

    public void whiteOn() throws IOException {
        sendCommand_(0x35);
    }

    public void whiteOff() throws IOException {
        sendCommand_(0x39);
    }

    public void allOn() throws IOException {
        Log.d("LEDBridge", "rgbOn()");
        rgbOn();
        Log.d("LEDBridge", "whiteOn()");
        whiteOn();
        Log.d("LEDBridge", "allOn() done");
    }

    public void allOff() throws IOException {
        Log.d("LEDBridge", "rgbOff()");
        rgbOff();
        Log.d("LEDBridge", "whiteOff()");
        whiteOff();
        Log.d("LEDBridge", "allOff() done");
    }

    private void sendCommand_(int first) throws IOException {
        new SendPacketTask(this).execute((byte) first, (byte) 0, (byte) 0x55);
    }
}
