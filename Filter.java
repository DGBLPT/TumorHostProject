import java.io.*;
import java.util.Set;

public class Filter {

    private static final Set<String> KEEP = Set.of(
        "transcript_ablation",
        "frameshift_variant",
        "stop_gained",
        "stop_lost",
        "start_lost",
        "missense_variant",
        "inframe_insertion",
        "inframe_deletion",
        "protein_altering_variant",
        "coding_sequence_variant",
        "splice_acceptor_variant",
        "splice_donor_variant",
        "splice_region_variant",
        "splice_donor_5th_base_variant",
        "splice_donor_region_variant",
        "splice_polypyrimidine_tract_variant",
        "5_prime_UTR_variant",
        "3_prime_UTR_variant"
    );

    public static void main(String[] args) throws IOException {

        if (args.length < 2) {
            System.out.println("Usage: java FilterVEP <input.txt> <output.txt>");
            return;
        }

        String inputFile = args[0];
        String outputFile = args[1];

        long totalRows = 0;
        long keptRows = 0;

        long startTime = System.nanoTime();

        try (
            BufferedReader in = new BufferedReader(
                new FileReader(inputFile),
                16 * 1024 * 1024
            );

            BufferedWriter out = new BufferedWriter(
                new FileWriter(outputFile),
                16 * 1024 * 1024
            )
        ) {

            String line;

            while ((line = in.readLine()) != null) {

                // Preserve VEP metadata/header lines
                if (line.startsWith("#")) {
                    out.write(line);
                    out.newLine();
                    continue;
                }

                totalRows++;

                // Find column 7: Consequence
                int p1 = line.indexOf('\t');
                int p2 = line.indexOf('\t', p1 + 1);
                int p3 = line.indexOf('\t', p2 + 1);
                int p4 = line.indexOf('\t', p3 + 1);
                int p5 = line.indexOf('\t', p4 + 1);
                int p6 = line.indexOf('\t', p5 + 1);
                int p7 = line.indexOf('\t', p6 + 1);

                if (p6 < 0 || p7 < 0) {
                    continue;
                }

                String consequence = line.substring(p6 + 1, p7);

                if (keepConsequence(consequence)) {
                    out.write(line);
                    out.newLine();
                    keptRows++;
                }

                if (totalRows % 1_000_000 == 0) {
                    System.out.printf(
                        "Processed %,d rows | kept %,d (%.2f%%)%n",
                        totalRows,
                        keptRows,
                        100.0 * keptRows / totalRows
                    );
                }                
            }
        }

        double seconds = (System.nanoTime() - startTime) / 1e9;

        System.out.printf("%nFinished%n");
        System.out.printf("Total rows: %,d%n", totalRows);
        System.out.printf("Kept rows:  %,d%n", keptRows);
        System.out.printf("Retained:   %.2f%%%n", 100.0 * keptRows / totalRows);
        System.out.printf("Time:       %.1f seconds%n", seconds);
    }

    private static boolean keepConsequence(String consequence) {

        int start = 0;

        while (true) {

            int comma = consequence.indexOf(',', start);

            String term;

            if (comma == -1) {
                term = consequence.substring(start);
            } else {
                term = consequence.substring(start, comma);
            }

            if (KEEP.contains(term)) {
                return true;
            }

            if (comma == -1) {
                break;
            }

            start = comma + 1;
        }

        return false;
    }
}