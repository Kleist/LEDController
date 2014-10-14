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
        RGB_1,
        RGB_2,
        RGB_3,
        RGB_4,
        WHITE_1,
        WHITE_2,
        WHITE_3,
        WHITE_4,
    }
    private static final int RETRANSMISSIONS = 3;
    private static final int WAIT_AFTER_TRANSMISSION_IN_MS = 50;

    private static final byte[] CMD_RGB_ON = {
            0x42, // All
            0x45, 0x47, 0x49, 0x4B, // Zone 0-3
    };
    private static final byte[] CMD_RGB_OFF = {
            0x41, // All
            0x46, 0x48, 0x4A, 0x4C, // Zone 0-3
    };
    private static final byte CMD_WHITE_ON = 0x35;
    private static final byte CMD_WHITE_OFF = 0x39;

    private static final HashMap<LED, ArrayList<Byte>> onCommands_;
    private static final HashMap<LED, ArrayList<Byte>> offCommands_;
    static {
        onCommands_ = new HashMap<LED, ArrayList<Byte>>();
        offCommands_ = new HashMap<LED, ArrayList<Byte>>();

        addCommands_(onCommands_, LED.ALL, CMD_RGB_ON[0], CMD_WHITE_ON);
        addCommands_(onCommands_, LED.RGB, CMD_RGB_ON[0]);
        addCommands_(onCommands_, LED.WHITE, CMD_WHITE_ON);
        addCommands_(onCommands_, LED.RGB_1, CMD_RGB_ON[1]);
        addCommands_(onCommands_, LED.RGB_2, CMD_RGB_ON[2]);
        addCommands_(onCommands_, LED.RGB_3, CMD_RGB_ON[3]);
        addCommands_(onCommands_, LED.RGB_4, CMD_RGB_ON[4]);

        addCommands_(offCommands_, LED.ALL, CMD_RGB_OFF[0], CMD_WHITE_OFF);
        addCommands_(offCommands_, LED.RGB, CMD_RGB_OFF[0]);
        addCommands_(offCommands_, LED.WHITE, CMD_WHITE_OFF);
        addCommands_(offCommands_, LED.RGB_1, CMD_RGB_OFF[1]);
        addCommands_(offCommands_, LED.RGB_2, CMD_RGB_OFF[2]);
        addCommands_(offCommands_, LED.RGB_3, CMD_RGB_OFF[3]);
        addCommands_(offCommands_, LED.RGB_4, CMD_RGB_OFF[4]);
    }

    private static void addCommands_(HashMap<LED, ArrayList<Byte>> map, LED led, byte... commands) {
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

    public void change(LED led, boolean stateOn) {
        if (stateOn) {
            sendCommands_(onCommands_.get(led));
        }
        else {
            sendCommands_(offCommands_.get(led));
        }
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
