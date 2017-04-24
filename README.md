# Automatic Disambiguation of French Discourse Connectives

Discourse connectives (e.g. however, because) are terms that can explicitly convey a discourse relation within a text. While discourse connectives have been shown to be an effective clue to automatically identify discourse relations, they are not always used to convey such relations, thus they should first be disambiguated between discourse-usage and non-discourse-usage. In this paper, we investigate the applicability of features proposed for the disambiguation of English discourse connectives for French. Our results with the French Discourse Treebank (FDTB) show that syntactic and lexical features developed for English texts are as effective for French and allow the disambiguation of French discourse connectives with an accuracy of 94.2%.

see [Laali and Kesseim 2015](https://arxiv.org/abs/1704.05162) for more information.

# Running the classifier

To complie this classifier, you need

1. [Java 1.8](http://www.oracle.com/technetwork/java/javase/overview/java8-2100321.html)
1. [Apache Maven](https://maven.apache.org/)

Please run the following commands to compile the source codes:
```bash
git clone https://github.com/mjlaali/french-dc-disambiguation.git
cd french-dc-disambiguation/
mvn package dependency:copy-dependencies -DskipTests
```

To label french discourse connectives, please run the following comman:
```bash
./run.sh -i INPUT_DIR -o OUTPUT_DIR
```

where INPUT_DIR is the input directory that contains only text files and the output directory is where the results are saved in the XML format.

