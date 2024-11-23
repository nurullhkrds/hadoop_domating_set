package org.example;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class DominatingSetHadoop {

    public static class DominatingSetMapper extends Mapper<Object, Text, IntWritable, IntWritable> {

        private final static IntWritable node = new IntWritable();
        private final static IntWritable neighbor = new IntWritable();

        @Override
        protected void map(Object key, Text value, Context context) throws IOException, InterruptedException {
            String line = value.toString();
            String[] parts = line.split(",");
            if (parts.length == 2) {
                int node1 = Integer.parseInt(parts[0].trim());
                int node2 = Integer.parseInt(parts[1].trim());

                node.set(node1);
                neighbor.set(node2);
                context.write(node, neighbor);

                node.set(node2);
                neighbor.set(node1);
                context.write(node, neighbor);
            }
        }
    }

    public static class DominatingSetReducer extends Reducer<IntWritable, IntWritable, IntWritable, Text> {

        @Override
        protected void reduce(IntWritable key, Iterable<IntWritable> values, Context context) throws IOException, InterruptedException {
            Set<Integer> neighbors = new HashSet<>();
            for (IntWritable val : values) {
                neighbors.add(val.get());
            }

            Map<Integer, Set<Integer>> adjacencyMatrix = new HashMap<>();
            adjacencyMatrix.put(key.get(), neighbors);

            int totalNodes = adjacencyMatrix.size();
            int[] dugumRengi = new int[totalNodes];
            int[] D = DominatingSetSparceMatrix.degrees(adjacencyMatrix, dugumRengi, totalNodes);
            int[] DA = DominatingSetSparceMatrix.degreeDA(adjacencyMatrix, dugumRengi, totalNodes);
            double[] MC1 = DominatingSetSparceMatrix.birinciMalatyaMerkezilik(adjacencyMatrix, D, 1.0, dugumRengi);
            double[] MC2 = DominatingSetSparceMatrix.ikinciMalatyaMerkezilik(adjacencyMatrix, D, DA, MC1, 1.0, dugumRengi);

            int selectedNode = DominatingSetSparceMatrix.maxMalatya2Dugum(adjacencyMatrix, MC2, dugumRengi);

            if (selectedNode == key.get()) {
                context.write(key, new Text("Dominating Node"));
            }
        }
    }

    public static void main(String[] args) throws Exception {
        Configuration conf = new Configuration();
        Job job = Job.getInstance(conf, "dominating set");
        job.setJarByClass(DominatingSetHadoop.class);
        job.setMapperClass(DominatingSetMapper.class);
        job.setReducerClass(DominatingSetReducer.class);
        job.setOutputKeyClass(IntWritable.class);
        job.setOutputValueClass(IntWritable.class);
        FileInputFormat.addInputPath(job, new Path("C:/Users/Nurullah/Desktop/hadoopdata/graph_data.txt"));
        FileOutputFormat.setOutputPath(job, new Path("C:/Users/Nurullah/output"));
        System.exit(job.waitForCompletion(true) ? 0 : 1);
    }
}

