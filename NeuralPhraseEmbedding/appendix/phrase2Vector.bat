java -Xmx1g -cp "./stanford-parser-2011-09-14/stanford-parser.jar;./stanford-parser-2011-09-14/ejml-0.23.jar;" edu.stanford.nlp.parser.lexparser.LexicalizedParser -outputFormat "penn" -sentences newline  ./stanford-parser-2011-09-14/grammar/englishRNN.ser.gz input.txt > parsed.txt

matlab -nodesktop run