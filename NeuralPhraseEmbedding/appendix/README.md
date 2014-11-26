# APPENDIX of this package

This appendix folder contains several files, that we used in replication process.

Because these are in appendix, they are not fully commented.

### SaveParams.m
This file contains weight matrix converter. This file does:

- Extract `We`(Word embedding) and `words`(Word indexes) from `vars.normalized.100.mat` and make it as `Words_normalized.json` with JSON object format.
For example,

```javascript
{
	"*UNKNOWN*":[Numbers...], ...
}
```

- Extract `W1`(Weight Matrix of phrase 1), `W2`(Weight Matrix of phrase 2) and `b1`(Bias column vector) from `params.mat` and make it as `params.json` with JSON object format.
For example,

```javascript
{
	"W1":[[Number in 1st row], [Number in 2nd row]... ],
	"W2":[[Number in 1st row], [Number in 2nd row]... ],
	"b1":[[Number],[Number]...]
}
```

Those extracted files are in `/src/kr/ac/kaist/ir/resource`.

### phrase2Vector.bat

This is a Windows version of `phrase2Vector.sh` in code of (Socher et al., NIPS 2011).

To run this file, you must do several things in the code of (Socher et al., NIPS 2011).

1. Update the files of `/stanford-parser-2011-09-14` (Keep the folder name)
2. Insert `englishRNN.ser.gz` into `/stanford-parser-2011-09-14/grammar`. You can find that file from `stanford-parser-3.4.0-models.jar` or higher.
3. Run the script. If MatLab opens, you need to type `run`. (Because pipelining in Windows is not properly works)

### input.txt, phrases.txt, outVectors.txt

These files are used when test `StanfordWrapper` class with `StanfordWrapperTester`.

- `input.txt` is used when generate other two files using `phrase2Vector.bat`.
- `input.txt` and `outVectors.txt` are input of `StanfordWrapperTester`.