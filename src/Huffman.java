import java.io.*;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;

public class Huffman {
    public static void main(String[] args) {
        String s = "aaaaaabbcccccccccccdddddefffffff";
        HashMap map = generateFrenquencyMap(s);
        Huffman huff = new Huffman();
        huff.transmit();
        huff.receive();

    }

    public static final String INPUTFILE = "C:\\Users\\itzst\\IdeaProjects\\HuffmanCodeProject\\src\\input.txt";
    public static final String ENCODEDFILE = "C:\\Users\\itzst\\IdeaProjects\\HuffmanCodeProject\\src\\binary.txt";
    public static final String OUTPUTFILE = "C:\\Users\\itzst\\IdeaProjects\\HuffmanCodeProject\\src\\output.txt";
    private HuffmanTree ht;
    private HashMap<Character, Integer> frequencyMap;
    public static final int ASCII_NEWLINE = 10;
    public static final int ASCII_SIZE = 256;

    public Huffman(){

        try {
            Path path = Paths.get(INPUTFILE);
            String s = Files.readString(path, StandardCharsets.US_ASCII);
            System.out.println(s);
            this.frequencyMap = generateFrenquencyMap(s);
            this.ht = new HuffmanTree(frequencyMap);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static HashMap<Character, Integer> generateFrenquencyMap(String s){
        HashMap<Character, Integer> map = new HashMap<Character, Integer>();
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            Integer val = map.get(c);
            if (val != null) {
                map.put(c, val + 1);
            }
            else {
                map.put(c, 1);
            }

        }
        return map;
    }

    private class Node implements Comparable<Node> {

        public final char CHARACTER;    // the character we want to transmit; if a non-leaf node, this element is set to null.
        public final boolean ISLEAF;
        public int frequency;           // how many times this character appeared our original sample
        public String binarySequence;   // "01001 ..." for this character
        public final Node LEFTCHILD;
        public final Node RIGHTCHILD;

        public Node(char character, int frequency, Node leftChild, Node rightChild) {
            this.CHARACTER = character;
            this.frequency = frequency;
            this.binarySequence = "";   // this will be updated later in the algorithm
            this.LEFTCHILD = leftChild;
            this.RIGHTCHILD = rightChild;
            ISLEAF = (leftChild == null && rightChild == null) ? true : false;
        }

        // configure compareTo method to create MIN heap
        public int compareTo(Node other) {
            if (this.frequency != other.frequency)
                return this.frequency - other.frequency;   // frequency is primary sorting criteria
            else
                return this.CHARACTER - other.CHARACTER;   // ASCII position is secondary critera
        }

        public String toString(){
            return CHARACTER + ":" + frequency;
        }
    }

    private class HuffmanTree {

        private PriorityQueue<Node> pq;

        public Map encodeMap;    // <char, String>
        public Map decodeMap;    // <String, char>

        // constructor
        public HuffmanTree(HashMap<Character, Integer> frequencyTable) {
            this.encodeMap = new HashMap<Character, String>();
            this.decodeMap = new HashMap<String, Character>();
            Comparator<Node> c1 = new Comparator<Node>() {
                @Override
                public int compare(Node o1, Node o2) {
                    if (o1.frequency != o2.frequency)
                        return o1.frequency - o2.frequency;   // frequency is primary sorting criteria
                    else
                        return o1.CHARACTER - o2.CHARACTER;   // ASCII position is secondary critera
                }
            };
            this.pq = new PriorityQueue(11, c1);

            for (Character character : frequencyTable.keySet()) {
                if (frequencyTable.get(character) > 0) {
                    pq.add(new Node(character, frequencyTable.get(character), null, null));
                }
            }
            // for each character in the frequency table that has a non-zero frequency, create a leaf Node
            // and add the Node to the priority queue
            /*while(!pq.isEmpty()){
                System.out.println(pq.poll());
            }*/

            while (pq.size() > 1) {
                Node smallNode = pq.poll(); // 8
                Node largeNode = pq.poll(); // 11
                pq.add(new Node((char) 0, smallNode.frequency + largeNode.frequency, smallNode, largeNode));
            }
            // now construct the binary tree that will be the Huffman tree
            // repeat the following until the priority queue is down to one Node only (the root Node)
            //	take the next two elements out of the priority queue
            //	combine them to make a new non-leaf Node
            // 	add the node to the priority queue


            // now construct the binary sequences for each character
            buildBinarySequence(getRoot(), "");
            System.out.println(encodeMap);
            System.out.println(decodeMap);
            //print2DUtil(getRoot(), 0, 10);
        }

        public Node getRoot() {
            // return the last Node remaining in the priority queue. Do not remove this Node from the priority queue
            return pq.peek();
        }

        private void buildBinarySequence(Node n, String binarySequence) {
            n.binarySequence = binarySequence;
            // set the given Node's (n's) binary sequence to the argument String (binarySequence).
            if (!n.ISLEAF) {
                buildBinarySequence(n.LEFTCHILD, binarySequence + "0");
                buildBinarySequence(n.RIGHTCHILD, binarySequence + "1");
            } else {
                encodeMap.put(n.CHARACTER, binarySequence);
                decodeMap.put(binarySequence, n.CHARACTER);
            }
        }

        private void print2DUtil(Node root, int space, int count) {
            // Base case
            if (root == null)
                return;

            // Increase distance between levels
            space += count;

            // Process right child first
            print2DUtil(root.RIGHTCHILD, space, count);

            // Print current node after space
            // count
            System.out.print("\n");
            for (int i = count; i < space; i++)
                System.out.print(" ");
            System.out.print(root + "\n");

            // Process left child
            print2DUtil(root.LEFTCHILD, space, count);
        }
    }

    public void transmit() {
        try {
            File binaryFile = new File(ENCODEDFILE);
            binaryFile.createNewFile();

            FileWriter writer = new FileWriter(binaryFile);
            BufferedReader file = new BufferedReader(new FileReader(INPUTFILE));
            String line = "";

            while ((line = file.readLine()) != null) {
                String transmitString = "";
                for (Character c : line.toCharArray()) {
                    String temp = this.ht.encodeMap.get(c).toString();
                    writer.write( temp  );
                    //System.out.println(temp);
                    // System.out.println(this.ht.encodeMap.get(c));
                }
                String encodedNewLineCharacter = this.ht.encodeMap.get((char)ASCII_NEWLINE).toString();
                writer.write(encodedNewLineCharacter);
            }
            file.close();

            writer.flush();
            writer.close();
        } catch (Exception e) {
            System.out.println("File Error handling: " + ENCODEDFILE);
        }
    }

    public String receive(String encodedBinary) {
        String originalMessage = "";
        String focus = "";
        int i = 0;
        while (i < encodedBinary.length()) {
            focus += encodedBinary.substring(i,i+1);
            if (this.ht.decodeMap.containsKey(focus)) {
                originalMessage += this.ht.decodeMap.get(focus);
                focus = "";
            }
            ++i;
        }
        return originalMessage;
    }

    public void receive() {
        try {
            BufferedReader encodedFile = new BufferedReader(new FileReader(ENCODEDFILE));
            File outputFile = new File(OUTPUTFILE);
            outputFile.createNewFile();
            FileWriter writer = new FileWriter(outputFile);

            String line = "";
            while ((line = encodedFile.readLine()) != null) {
                String outputLine = receive(line);
                // replace '\n' characters with Windows/Mac Newline characters
                // otherwise, we will not get separate lines in output.txt
                outputLine = outputLine.replaceAll("\n", System.lineSeparator());
                writer.write(outputLine);

            }
            encodedFile.close();
            writer.flush();
            writer.close();
        } catch (Exception e) {
            System.out.println("Error handling file: " + INPUTFILE);
        }
    }
}
