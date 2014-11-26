Neural Phrase Embedding v0.0.1-SNAPSHOT
=======================================

## Overview

This package includes JAVA implementation of [(Socher et al., NIPS 2011)](http://www.socher.org/index.php/Main/DynamicPoolingAndUnfoldingRecursiveAutoencodersForParaphraseDetection), and intermediate vector extraction of [(Socher et al., ACL 2013)](http://www.socher.org/index.php/Main/ParsingWithCompositionalVectorGrammars).

Currently, this contains just __trained results__ of the paper, but we planned to add other implemetations, such as:

- Stacked Denoised Auto Encoder
- Recursive Auto Encoder *in (Socher et al., NIPS 2011)*
- Unfolding Recursive Auto Encoder *in (Socher et al., NIPS 2011)*

## Usage

### Run Demo Program
To see how it works, you can simply run the jar file, with following shell script on root directory:

```
java -jar ./NPE-latest-jar-with-dependencies.jar
```

Then it automatically launches StanfordWrapper class. All you need to do is type the phrase when `Phrase?` prompt is shown.

### Javadocs
This package contains javadoc jar file, `NPE-latest-javadoc.jar`.

### Basic Usage
Here is the sample code of StanfordWrapper `main` method

```java
	public static void main(String[] args) {
		final StanfordWrapper instance = StanfordWrapper.getInstance();

		final Scanner scan = new Scanner(System.in);
		String line;
		do {
			System.out.print("Phrase? ");
			line = scan.nextLine();
			// Generate Parse Tree from Stanford RNN Parser.
			final Tree tree = instance.parseTree(line);
			// Get Phrase Vector(URAE)
			final SimpleMatrix matrix = instance.getPhraseVectorOf(tree);
			// If you want to get Syntactic Phrase Vector,
			// final SimpleMatrix matrix = instance.getSyntacticScoreVectorOf(tree);
			// If you want to get Syntactic Weight Matrix of that,
			// final SimpleMatrix matrix = instance.getSyntacticWeightMatrixOf(tree);
			System.out.println(matrix.toString());
			System.out.println(tree.toString());
		} while (line.length() > 0);
		scan.close();
	}
```