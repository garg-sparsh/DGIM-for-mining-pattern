package ProgrammingAssignments;
import java.io.*;
import java.net.ConnectException;
import java.net.Socket;
import java.util.Iterator;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.List;
import java.util.LinkedList;
import java.util.concurrent.Semaphore;
import java.util.regex.Pattern;
import java.util.Scanner;
import java.net.InetAddress;
import java.net.UnknownHostException;

public class P2 {
    public static BlockingDeque<Bucket> queue = new LinkedBlockingDeque<>();
    public static List<Integer> window = new LinkedList<>();
    public static int window_size;
    public static String hostname;
    public static int port;
    public static Semaphore mutex = new Semaphore(1);
    public static Thread t2;
    private static final Pattern PATTERN = Pattern.compile(
            "^(([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\.){3}([01]?\\d\\d?|2[0-4]\\d|25[0-5])$");

    public static void main(String... args) throws InterruptedException, IOException {
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(System.in));
        String s1 = bufferedReader.readLine();
        try {
            if (!s1.startsWith("#")) {
                if (s1.contains("#")) {
                    window_size = Integer.parseInt(s1.substring(0, s1.indexOf("#")));
                } else {
                    window_size = Integer.parseInt(s1);
                }
            } else {
                window_size = Integer.parseInt(bufferedReader.readLine());
            }
            if(window_size!=0) {
            s1 = bufferedReader.readLine();
            if (s1.startsWith("#")) {
                s1 = bufferedReader.readLine();
            } else if (s1.contains("#")) {
                s1 = s1.substring(0, s1.indexOf("#"));
            }
                String str[] = s1.split(":");
                if (str[0].equals("localhost") || str[0].equals("127.0.0.1")) {
                    hostname = "localhost";
                    if (Integer.parseInt(str[1]) > 1024 && Integer.parseInt(str[1]) < 65535) {
                        port = Integer.parseInt(str[1]);
                    } else {
                        System.out.println("Aborted as port was unavailable");
                    }
                    new Thread(new BucketingThread(queue, window, hostname, port, window_size)).start();
                    t2 = new Thread((new CountingThread(queue, window, window_size)));
                } else {
                    if (validate(str[0])) {
                        InetAddress adress = InetAddress.getByName(str[0]);
                        hostname = adress.toString().split("/")[1];
                        if (Integer.parseInt(str[1]) > 1024 && Integer.parseInt(str[1]) < 65535) {
                            port = Integer.parseInt(str[1]);
                        } else {
                            System.out.println("Aborted as port was unavailable");
                        }
                        new Thread(new BucketingThread(queue, window, hostname, port, window_size)).start();
                        t2 = new Thread((new CountingThread(queue, window, window_size)));
                    }
                }
            }else{
                throw new NumberFormatException();
            }
         }
        catch (NumberFormatException nfe) {
            System.out.println("Aborted as window size/port is incorrect...");
        }catch(ArrayIndexOutOfBoundsException io){
            System.out.println("Aborted as window size/port is incorrect...");
        }catch (NullPointerException npe){
            System.out.println("Socket is not connected");
        }
    }

    public static boolean validate(String ip) throws UnknownHostException {
	InetAddress adress = InetAddress.getByName(ip);
	if(PATTERN.matcher(adress.toString().split("/")[1]).matches()){
        return true;}
      else{
	    return false;
    }
    }
}

class BucketingThread implements Runnable {
    public BlockingDeque<Bucket> queue = null;
    public List<Integer> window = null;
    public String hostname;
    public int port;
    public int window_size;
    protected static int index;
    public static Socket socket;
    public BucketingThread(BlockingDeque<Bucket> queue, List<Integer> window,String hostname, int port,int window_size) {
        this.queue = queue;
        this.window = window;
        this.hostname=hostname;
        this.port=port;
        this.window_size = window_size;
    }

    private void bucketing(int index, int str) {
        Bucket bucket = new Bucket(index, str);
        queue.addFirst(bucket);
        Iterator<Bucket> iter = queue.iterator();
        Boolean hey = checkMerge(iter);
        if (hey) {
            Iterator<Bucket> iter1 = queue.iterator();
            merging(iter1,true);
        }
    }

    private void merging(Iterator<Bucket> iter,boolean isMerge) {
        if(isMerge){
            iter.next();
            iter.next();}
        else{
            iter.next();
            iter.next();
            iter.next();
        }
        Bucket first = iter.next();
        Bucket next = iter.next();
        int size = first.getSize();
        first.setSize(first.getSize() + next.getSize());
        iter.remove();
        Iterator<Bucket> iter1 = getIterator(size);
        Boolean hey2 = checkMergeFor33(iter1);
        if (hey2) {
            Iterator<Bucket> iter2 = getIterator(size);
            merging(iter2,false);
        }
    }

    private Iterator<Bucket> getIterator(int size) {
        Iterator<Bucket> iter = queue.iterator();
        while (iter.hasNext()) {
            Bucket bucket = iter.next();
            if (bucket.getSize() == size)
                return iter;
        }
        return null;
    }

    private boolean checkMerge(Iterator<Bucket> iter) { // 2 static boolean loops try
        Bucket first, second, third, fourth;
        if (iter.hasNext()) {
            first = iter.next();
            if (iter.hasNext()) {
                second = iter.next();
                if (second.getSize() != first.getSize())
                    return false;
                else {
                    if (iter.hasNext()) {
                        third = iter.next();
                        if (second.getSize() != third.getSize())
                            return false;
                        else {
                            if (iter.hasNext()) {
                                fourth = iter.next();
                                if (third.getSize() == fourth.getSize())
                                    return true;
                                else
                                    return false;
                            } else
                                return false;
                        }
                    } else
                        return false;
                }
            } else
                return false;
        } else
            return false;
    }

    private boolean checkMergeFor33(Iterator<Bucket> iter) {// 2 static boolean loops try
        if (iter.hasNext()) {
            iter.next();
            return checkMerge(iter);
        } else
            return false;
    }
    @SuppressWarnings("deprecation")
    public void run() {
        try {
            socket = new Socket(P2.hostname, P2.port);
            P2.t2.start();
            DataInputStream in = new DataInputStream(socket.getInputStream());
            String str3;
            while (!(str3=in.readLine()).equals(null)) {
                index++;
                P2.mutex.acquire();
                System.out.print(str3 + " ");
                if (Integer.parseInt(str3) == 1)
                    bucketing(index, Integer.parseInt(str3));
                window.add(Integer.parseInt(str3));
                if (window.size() > window_size) {
                    window.remove(0);
                }
                P2.mutex.release();
            }
            System.out.println();
        } catch(NullPointerException npe){
            System.out.println();
        }catch(InterruptedException ie){
            System.out.println("\nSome interrupted came, please try again");
        }catch(ConnectException ce){
            System.out.println("Incorrect Port");
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }
}

class CountingThread implements Runnable {
    public BlockingDeque<Bucket> queue = null;
    public List<Integer> window = null;
    public int window_size;
    public CountingThread(BlockingDeque<Bucket> queue,List<Integer> window,int window_size) {
        this.queue = queue;
        this.window = window;
        this.window_size = window_size;
    }

    public void run() {
        try {
            System.out.println();
           // Scanner br = new Scanner(new InputStreamReader(System.in)).useDelimiter("\n");
            BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
            String s="";
            String input_query = "What is the number of ones for last <k> data?";
            while (true){
                int check = 0;
                int total = 0;
                int addValue = 0;
                int firstChar;
                String entry = "W";
                if((firstChar=br.read())!='\0'){
                    P2.mutex.acquire();
                    s=br.readLine();
                    if(firstChar==87|| firstChar == 119){
                        s=entry+s;
                    }
                    P2.mutex.release();
                    if(s.split(" ").length==10){
                        for (int i = 0; i < 8; i++) {
                            if (s.split(" ")[i].equals(input_query.split(" ")[i])) {
                                check++;
                            }
                        }}
                }
                if (check == 8 && s.split(" ")[9].equals(input_query.split(" ")[9])) {
                    Iterator<Bucket> iter = queue.iterator();
                    if(Integer.parseInt(s.split(" ")[8])<window_size){ //window_size=10
                        if(window.size()-Integer.parseInt(s.split(" ")[8])>=0){
                            for(int i=window.size()-1;i>=window.size()-Integer.parseInt(s.split(" ")[8]);i--){
                                if(window.get(i)==1){
                                    total++;
                                }
                            }
                            P2.mutex.acquire();
                            System.out.println("\nThe number of ones of last "+s.split(" ")[8]+" is exact "+total);
                            P2.mutex.release();}
                        else {
                            System.out.println("\nYour query is out of limit");
                        }
                    }else{
                        while (iter.hasNext()) {
                            Bucket bucket = iter.next();
                            int num = Integer.parseInt(s.split(" ")[8]);
                            if (bucket.getIndex()+num>BucketingThread.index) { // k=20 (to check in buckets
                                addValue = bucket.getSize();
                                total += addValue;
                            } else
                                break;
                        }
                        total -= addValue / 2;
                        P2.mutex.acquire();
                        System.out.println("\nThe number of ones of last "+s.split(" ")[8]+" is estimated "+total);
                        P2.mutex.release();
                    }
                } else {
                    P2.mutex.acquire();
                    System.out.println("\nYour query is wrong, please try again");
                    P2.mutex.release();
                }
            }
        }catch (ArrayIndexOutOfBoundsException aie){
            System.out.println("\nYour query is wrong, please try again");
        }catch(InterruptedException ie){
            System.out.println("\nSome interrupted came, please try again");
        }catch(IOException io){
            System.out.println("Something goes wrong");
        }
    }
}
class Bucket{
    private int index;
    private int size;

    public Bucket(int index, int size) {
        this.index = index;
        this.size = size;
    }

    public long getIndex() {
        return index;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }
}


