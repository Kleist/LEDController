package dk.andreaskleistsvendsen.ledcontroller;

import android.os.AsyncTask;
import android.util.Log;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.lang.Thread;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;

public class LEDBridge {
    enum LED {
        ALL,
        RGB,
        WHITE,
//        RGB_0,
//        RGB_1,
//        RGB_2,
//        RGB_3,
//        WHITE_0,
//        WHITE_1,
//        WHITE_2,
//        WHITE_3
    }
    private static final int RETRANSMISSIONS = 3;
    private static final int WAIT_AFTER_TRANSMISSION_IN_MS = 50;

    private static final byte CMD_RGB_ON = 0x42;
    private static final byte CMD_RGB_OFF = 0x41;
    private static final byte CMD_WHITE_ON = 0x35;
    private static final byte CMD_WHITE_OFF = 0x39;

    private static final HashMap<LED, ArrayList<Byte>> onCommands_;
    private static final HashMap<LED, ArrayList<Byte>> offCommands_;
    static {
        onCommands_ = new HashMap<LED, ArrayList<Byte>>();
        offCommands_ = new HashMap<LED, ArrayList<Byte>>();
        addCommands(onCommands_, LED.ALL, CMD_RGB_ON, CMD_WHITE_ON);
        addCommands(onCommands_, LED.RGB, CMD_RGB_ON);
        addCommands(onCommands_, LED.WHITE, CMD_WHITE_ON);
        addCommands(offCommands_, LED.ALL, CMD_RGB_OFF, CMD_WHITE_OFF);
        addCommands(offCommands_, LED.RGB, CMD_RGB_OFF);
        addCommands(offCommands_, LED.WHITE, CMD_WHITE_OFF);
    }

    private static void addCommands(HashMap<LED,ArrayList<Byte>> map, LED led, byte... commands) {
        ArrayList<Byte> array = new ArrayList<Byte>();
        for (byte cmd : commands) {
            array.add(cmd);
        }
        map.put(led, array);
    }


    private final InetAddress ip_;
    private final int port_;
    public enum LEDState {
        UNKNOWN_ERROR,
        DONE
    }

    private class SendPacketTask extends AsyncTask<Void, Void, LEDState> {
        private final ArrayList<ArrayList<Byte>> commands_;
        private LEDBridge parent_;
        public SendPacketTask(LEDBridge parent, ArrayList<ArrayList<Byte>> commands) {
            parent_ = parent;
            commands_ = commands;
        }

        @Override
        protected LEDState doInBackground(Void ... unused) {
            DatagramSocket socket = null;
            DatagramPacket[] packets = new DatagramPacket[commands_.size()];
            for (int i=0; i<commands_.size() ; ++i) {
                StringBuffer byteString = new StringBuffer();
                ArrayList<Byte> command = commands_.get(i);
                byte[] data = new byte[command.size()];
                packets[i] = new DatagramPacket(data, data.length, ip_, port_);
                for (int j=0; j<command.size(); ++j) {
                    data[j] = command.get(j);
                    byteString.append(String.format("%x,", command.get(j)));
                }
                Log.d("Packet", String.format("Preparing packet(hex): %s", byteString.toString()));
            }
            try {
                socket = new DatagramSocket();
                for (int i=0; i<RETRANSMISSIONS; ++i) {
                    for (int pI = 0; pI < packets.length; ++pI) {
                        socket.send(packets[pI]);
                        Thread.sleep(WAIT_AFTER_TRANSMISSION_IN_MS);
                    }
                }
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

    public void on(LED led) {
        sendCommands_(onCommands_.get(led));
    }

    public void off(LED led) {
        sendCommands_(offCommands_.get(led));
    }

    public void rgbOn() {
        on(LED.RGB);
    }

    public void rgbOff() {
        off(LED.RGB);
    }

    public void whiteOn() {
        on(LED.WHITE);
    }

    public void whiteOff() {
        off(LED.WHITE);
    }

    public void allOn() {
        on(LED.ALL);
    }

    public void allOff() {
        off(LED.ALL);
    }

    private void sendCommands_(ArrayList<Byte> commands) {
        Log.d("LEDBridge", String.format("sendCommands_(%d commands)", commands.size()));
        ArrayList<ArrayList<Byte>> commandBytes = new ArrayList<ArrayList<Byte>>();
        for (int i = 0; i<commands.size(); ++i) {
            Log.d("LEDBridge", String.format("sendCommands_(i) = %x", commands.get(i)));
            ArrayList<Byte> command = new ArrayList<Byte>(3);
            command.add(commands.get(i));
            command.add((byte)0);
            command.add((byte)0x55);
            commandBytes.add(command);
        }
        new SendPacketTask(this, commandBytes).execute();
    }
}
