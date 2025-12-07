package minhcrafters.chess.game.ai;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.concurrent.CompletableFuture;

public class UciEngine {
    private Process process;
    private BufferedReader reader;
    private BufferedWriter writer;
    private final String enginePath;

    public UciEngine(String enginePath) {
        this.enginePath = enginePath;
    }

    public void start() throws IOException {
        ProcessBuilder pb = new ProcessBuilder(enginePath);
        this.process = pb.start();
        this.reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        this.writer = new BufferedWriter(new OutputStreamWriter(process.getOutputStream()));

        sendCommand("uci");
        
        // Wait for uciok
        String line;
        while ((line = reader.readLine()) != null) {
            if (line.equals("uciok")) {
                break;
            }
        }
        
        sendCommand("isready");
        while ((line = reader.readLine()) != null) {
            if (line.equals("readyok")) {
                break;
            }
        }
    }

    public void sendCommand(String command) throws IOException {
        if (writer == null) return;
        writer.write(command + "\n");
        writer.flush();
    }

    public CompletableFuture<String> getBestMove(String fen, long wtime, long btime, long winc, long binc) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                sendCommand("position fen " + fen);
                sendCommand(String.format("go wtime %d btime %d winc %d binc %d", wtime, btime, winc, binc));

                String line;
                while ((line = reader.readLine()) != null) {
                    if (line.startsWith("bestmove")) {
                        return line.split(" ")[1];
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        });
    }

    public void close() {
        if (process != null) {
            process.destroy();
        }
    }
}
