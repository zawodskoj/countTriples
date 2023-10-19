package benchmark;

import jdk.incubator.vector.IntVector;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;

@State(Scope.Benchmark)
public class CountTriplesBenchmark {
    int[] arr;

    @Setup
    public void prepare() {
        arr = new int[1024];

        for (int i = 0; i < arr.length; i++) {
            arr[i] = (int) (Math.random() * 3);
        }
    }

    public int countTriples(int[] arr, int sum) {
        var n = arr.length;
        var count = 0;

        var species = IntVector.SPECIES_256;
        var vCount = species.length();

        var sumVector = IntVector.broadcast(species, sum);

        for (int i = 0; i < n; i++) {
            for (int j = i + 1; j < n; j++) {
                var ijSum = arr[i] + arr[j];
                var ijSumVector = IntVector.broadcast(species, ijSum);

                var k = j + 1;

                for (; k < n - vCount; k += vCount)
                {
                    var kVector = IntVector.fromArray(species, arr, k);
                    var ijkSumVector = kVector.add(ijSumVector);

                    var subResult = sumVector.eq(ijkSumVector);
                    if (subResult.anyTrue()) {
                        count += subResult.trueCount();
                    }
                }

                for (; k < n; k++)
                    count += ijSum + arr[k] == sum ? 1 : 0;
            }
        }

        return count;
    }

    @Benchmark
    public int countTriples() {
        return countTriples(arr, 0);
    }
}