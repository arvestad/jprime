# DLRS and Delirious #

![http://jprime.googlecode.com/files/DLRS_3.png](http://jprime.googlecode.com/files/DLRS_3.png)

## CONTENTS ##
  1. [Introduction](DLRS#INTRODUCTION.md)
  1. [Download](DLRS#DOWNLOAD.md)
  1. [References](DLRS#REFERENCES.md)
  1. [Workflow](DLRS#WORKFLOW.md)
  1. [Input](DLRS#INPUT.md)
  1. [Output](DLRS#OUTPUT.md)
  1. [Running](DLRS#RUNNING.md)
  1. [Analysis](DLRS#ANALYSIS.md)
  1. [Options](DLRS#OPTIONS.md)
  1. [Examples](DLRS#EXAMPLES.md)
  1. [FAQ](DLRS#FAQ.md)


---


---


---



## INTRODUCTION ##

**DLRS [=the model]** (previously known as GSR and GSRf) is a phylogenetic model for the evolution of a guest tree (_gene family_) inside a host tree (_species tree_). It models duplication and loss events, and sequence evolution with a relaxed molecular clock.

The evolution of the guest tree _G_ over the host tree _S_ can be envisioned as occurring in separate phases:
  1. _G_ is created by evolving down _S_ by means of duplications and loss events according to a birth-death-like process. Lineages branch deterministically at speciations.
  1. Using the clock-like times of _G_, relaxed branch lengths are obtained by multiplying each branch timespan with an _iid_ substitution rate according to a suitable distribution.
  1. Using _G_ and its branch lengths, sequence evolution occurs over the tree according to a substitution model of choice to produce guest family sequences. Possibly, site rate variation occurs according to a gamma distribution.

Hence the name DLRS = duplications, losses, rates & sequence evolution.

**JPrIME-DLRS [=the software application]**, colloquially known as **Delirious**, is the Java application corresponding to the DLRS model, and is used for inferring an unknown guest tree in light of a known and dated host tree and known sequence data for the leaves. The method relies on Bayesian inference using an MCMC framework. Thus, it will yield a posterior distribution of guest tree topologies and remaining parameters.

**Input:**
  * a file with a dated host tree.
  * a file with a multiple sequence alignment (MSA) of guest tree leaf sequences.
  * a file with a simple map relating the guest tree leaves to host tree leaves.

**Output:**
  * a file with samples drawn from the posterior distribution.
  * a supplementary info file with settings, proposal acceptance ratios, etc.


**Below, you will find information on the input and output of the application, while explanations of vital program options and some sample files can be found at the end of the document. If you want to get started quickly, we suggest that you download and try [Example 1](DLRS#EXAMPLES.md), and have a look at it while reading through the other parts of the guide.**


---


---


---


## DOWNLOAD ##

DLRS is not distributed as a binary, but is included in the JPrIME JAR file, which can be obtained from the [downloads page](http://code.google.com/p/jprime/downloads).


---


---


---



## REFERENCES ##

DLRS has been published [here](http://bioinformatics.oxfordjournals.org/content/early/2012/09/14/bioinformatics.bts548). If you use it, please cite:

_Sjöstrand, J, Sennblad, B, Arvestad, L, Lagergren, J (2012) DLRS: Gene tree evolution in light of a species tree. Bioinformatics. 28(22):2994-2995._


---


---


---


## WORKFLOW ##
A typical workflow may look like this. More details are found below.

![http://jprime.googlecode.com/files/DLRS_workflow.png](http://jprime.googlecode.com/files/DLRS_workflow.png)



---


---


---



## INPUT ##

### Host tree ###
Obtain a dated, rooted, bifurcating species tree in Newick format. The divergence times may be specified as branch lengths, e.g.:
```
(((Oryctolagus_cuniculus:89.9, (Cavia_porcellus:78.3,Mus_musculus:78.3):11.6):11.8, (Equus_caballus:80.8, (Felis_catus:55.7, Canis_lupus:55.7):25.1):20.9):62.2, Monodelphis_domestica:163.9):41.0;
```
Alternatively, one can specify absolute times using special formatting:
```
(((Oryctolagus_cuniculus[&&PRIME NT=0], (Cavia_porcellus[&&PRIME NT=0], Mus_musculus[&&PRIME NT=0])[&&PRIME NT=78.3])[&&PRIME NT=89.9], (Equus_caballus[&&PRIME NT=0], (Felis_catus[&&PRIME NT=0], Canis_lupus[&&PRIME NT=0])[&&PRIME NT=55.7])[&&PRIME NT=80.8])[&&PRIME NT=101.7], Monodelphis_domestica[&&PRIME NT=0])[&&PRIME NT=163.9][&&PRIME TT=41.0 NAME=Mammalia];
```
The host tree must be clock-like (ultrametric), with time 0 at the leaves and time >0 at the root, and a "stem" with timespan >0 (in the above example this was set to 41.0). The scale of the dates can be any unit: **they will be rescaled internally prior to running so that the root time is 1.0, and the output will refer to this scaling**, and not the original values. The scale factor is included in the info output file. The algorithm will use a discretization of the host tree. The default settings may be changed according to the options shown below.


![http://jprime.googlecode.com/files/Delirious_discretization_2.png](http://jprime.googlecode.com/files/Delirious_discretization_2.png)


A good resource for divergence times is http://www.timetree.org. If you cannot find references to reliable divergence times, you will need to date the tree yourself. Typically, this is achieved by obtaining a set of "core" gene families believed to be congruent with the species tree (monocopy syntenic orthologs, housekeeping genes, etc.). These can then be aligned, and divergence times be acquired with a suitable application using a concatenated multiple locus sequence alignment (MLSA), i.e., a _supergene_.
Some programs may allow the different loci to not be concatenated and to treat the partitions with separate model parameters. For dating, consider e.g. [BEAST](http://beast.bio.ed.ac.uk), [MAP-DP](http://www.biomedcentral.com/1471-2148/8/77), [PhyloBayes](http://www.phylobayes.org) or [r8s](http://loco.biosci.arizona.edu/r8s).

### Multiple sequence alignment and substitution model ###
The multiple sequence alignment (MSA) can be computed with any suitable program (MAFFT, MUSCLE, CLUSTAL, etc.), but should be provided on the FASTA format, e.g. for the example above:
```
>Canis_lupus_1
LVCRFITVIFDLYDDFNNFLDILKAYKGMFSNDYCYNMLLQRRGVRFAIPGNSQHLVNWLKLIGARVEPAASSVFHASTSWDQRKTSIGKGHGIYYEYGNVLSDAMAVQCEILGGHADLTYQKDGIGNRVLITVVRYMDRYAADNLRSWQVEALDGGQLTDMCAVICGYEDEGTCRKALPNLDRKATGEDLFKVKSSRRFTTNVMSFFHQTSERTETTIVEVPDLNCVCPDITVGLLQTPSMEIWAPTRIPGSIMKGEASSNSKDMSVWDGIIATTQLVGDNNCYFLEARKALPNPSAFP
>Cavia_porcellus_1
VLCRFLTVIFALYDDFNRFLDILKAYKGMFSNCYCYNMLVTRRGVRFAIPGNSQHLVNWLKTIGGRVEPAAANVFEANTSWDQRKTSIGKDHGIYYEYGNTTPVAMAVQCDILGDHAGLTYQQDGIGNRVNITVLGYMDRYAVDTLLVWQVQIFDGGQYTDMCAVVCGYSDEYTCRKTLPNLDRKASGEDVFKAKVSRRFTINVMSFFHQTSERTKTVGVEVPDLNCVCSGTTAGLLQTPSMEKWAPTKMSSAIMKGEGGSNKSDMSVGVVIVATLQLTGDANCYFLETRKVLLNPSAFP
>Cavia_porcellus_2
TLCRFMTVIFNAYDDFNRFLDILISYKGMFADRYCLNMLLTRRGVRFTLPGNSQHLVNWLKTIGGRVKPRANSIFEANTSWDQRKTSIGKDHGIYYEYGNKVSVAMAVQCEILGEHADLTYQRDGIGNRISITVMPYMDRYAIDALRSWQVKIFDGTQYTEMCVVVCGYGDEYTCRKTLPNLDRVASAEDVFKSKLSARFFINSMSLFHQTSKRTETTGVEVPDLNCVCSGTTVGLLQTPSMEKWAPVKIGSAIMQGEAGSNTSDMSVVVAIVVTPQLTGDANCYFLQTRKVLLNPSAFP
>Cavia_porcellus_3
VLCRFITTIFALYGDFNRFLDVVRAYKGMFSNCYCYNMLLTRQALRFGIPGNSQHLVGWLKTIGGRVEPVASSAFEANTSWDQRKTSIGKDHGIYYEYGNTVSVTMAVQCDILGDHADLTYQKDGIGNRVSITVLGYMDQYAIDRLRSWQVQIFDGGQYADMCSVVCGYTDNNTCRKILPNVDRKASGEDVFKSKKSRRFTPNVMSFFHQTSERAETVGVEMPDLNCVCSGATAGLLQTPSMEKWAPNKISSAIMKGEAGSNTSDMVVGAGIVATLQLTGDANCYFVEGRKVLPNPSSFP
>Equus_caballus_1
IVCRFMTVIFALYDDFNSFLGILKVYKGMFGNCYCFNMLLKRQSVRFGISGNSQHLVNWLKRIGGRVEPAASNVFEGVTSWDQRKTSIGKDHGIYYEYGNQKNVAMAVQCDLLGDHAGLTYQREGIGDRVSITVRRYMDRYAVDGLRSWQVRLMAGGQFADMCAVVCGYTDEYSCRKTLPNTDRNATGENVFKVRNSRRFSSNVMSFFHQASEDTESNGAEVPDLNCVCSGTTAGLLQTPTMEKWTPIKMVSAIMKGEAGVNSKDMSIGDGIIATIQLLGDSNRYSLETREVLPNPSVFP
>Felis_catus_1
LTCRFVTVIFDLYDDFNNFLDILKAYKGMFSNCYCYNMLLQRRSVRFAIPGNSQHLVNWLKLIGGRVEPAASSVFAASTSWDQRKTSIGKGHGIYYEYGNNVGDAMAVQCDILGGHADLTYQKDGIGNRVLITVLRYMDRYAADGLRSWAVLVLDGGQFTDECAIVCGYADECTCRETLPNLDRKASGEDLFKIKSSRQFTTNVMSFFHQSSERQETSGVEVPDLNCVCSDDTAGLLQTPSMEIWAPTRIPGSIMKLEAGKNAKGMSVWDGIIATLQLVGDTNCYFLEARKALPNPSAFP
>Felis_catus_2
LTCRFVTVIFDLYDDFNNFLDILKAYKGMFSNCYCYNMLLQRRSVRFAIPGNSQHLVNWLKLIGGRVEPAASSVFAASTSWDQRKTSIGKGHGIYYEYGNNVGDVMAVQCDILGGHADLTYQKDGIGNRVLITVLRYMDRYAADGLRSWAVLVLDGGQFTDECAIVCGYADECTCRETLPNLDRKASGEDLFKIKSSRQFTTNVMSFFHQSSERQETSGVEVPDLNCVCSDNTAGLLQTPSMEIWAPTRIPGSIMKLEAGKNAKGMSVWDGIIATLQLVGDTNCYFLEARKALPNPSAFP
>Monodelphis_domestica_1
IVCRIITDIFALYYDFDRFLDILIRYKGMFSNCYCYNMLMARAGVRFEIPKNSRHLVPWLKTIGGRVEPGANSVFEAVTTWDQRKTSISKAPGIYYEYGNTESAAMAVQCNILGDHADLIYQRDGIGVRVSITVPRYIDRYAVDALRSWKVQKFDGGEYADMCPVLCAYSDDYTCRKPLPNMDKKTSGENLFKSKRSRRFTTNIMSFFHQIKQRTEANVIEVADLNCVCSNTTAALVQTPSMENWAPTKISSAIMKGEAASNTKDMNVVVGIVATLQLVGDANCYFFEQRKVLPNPSAFP
>Mus_musculus_1
VLCRFISVIFALYDDFSRFLDILKLYKGMFSNCYCYNMLLTRRGVRFGIPGNSQHLVNWLKTIGGRVEPAAASVFEANTSWDQRKTSIGKDHGIYYEYGNTISVAMAVQCDILGDHADLTYRKDGIGNRVSITIVGYMDRYAVDRLRVWQVQIFDGGQYTEICSVVCGYTDEYTCRKPLPKLDRKASGEALFKAKVSRRFTTNVMSFFHQASNRTETVGVEDPDLNCVCSGTTAGLLQTPSMEKWAPTKIVSAIMKGDAVSNTSNMTVGSVIIATLQLTADANCYFVDARKVLLNPSAFP
>Mus_musculus_2
VLCRFITAIFALFEDFNRFLDILRAYKGMFSNCYCYNMVLTRQGIRFRIRGNSDHLVGWLKTIGGRVEPTASSVFQANTSWDQRKTSIGKDHGIYYEYGNAISVTMAVQCDILGDDADLTYQKDGIGNRVSITVARYMDRYAVDPLRSWQVQVFDGGQYVDMCVVVCGYTDEDTCRHILPNVERKATGENVFKGKKSRRFTTNAMSFFHQTSDRTETSGVEMPDLNCVCSVATGGVLQTPSMEKWAPTKITSAIMKGEAGTNPSDMVVGDLITATLQWTGDANCYFLETRKVLPNPSSFP
>Oryctolagus_cuniculus_1
TLCRFISVIFALYADFNRFLEILKVYVGMISNDYCYNMLLTRQVLRFGIPGNSQHLVNWLKTIGTRVEPGASSVFEADTSWDERKTSIGKDHGVYYEYGNTVSVAMSVQCDILGDHADLTYQKDGIGNRVSTNDLRYMDRYAVDQLRSWQVGMFDGGNYTEMNSVVCGYFDGYTCRVALPNMNRAASGEDVFRAKASRFFTANVMSFFHQTSERTETIGVEVPDLNCACSGTLTGLLQTPSMEVWARTNISSAIMKGQSGSNISDMSVGVTIVATLQLTADANCYFLETRVLLVNRSAFP
>Oryctolagus_cuniculus_2
DVCRFITVIFALYDDFNRFLNIERAYKGMFSNCYCYNMLLTRQGVRFGIPGNSQHLVGWLKTIGVRVDAVAVSAFEANTSWDQRKTSIGKDHGIYYEYANTVSVTMAVQCDILGDHADLTYQRDGIGNLVSITVVSYMDRYKVDRLRSWQVQIFDGGQLSDMCAVVCGYNDEDKCRKILPNVDRKASGENMFKVKRLRRFTANVMSFFHQTSERTETVRVEMPDLNCVCPGATAGLLQTPSMEVWAAQKISSAIMKGETARNTSDMSVGVSIVATLQLSGDANCYFIETRKVLPNPSSFP
```
Consider clearing ambiguous positions from the alignment before feeding it into Delirious to improve performance. There are many such applications, e.g., [trimAl](http://trimal.cgenomics.org), [ZORRO](http://sourceforge.net/projects/probmask/), [Gblocks](http://molevol.cmima.csic.es/castresana/Gblocks.html), [GUIDANCE](http://guidance.tau.ac.il) and [ALISCORE](http://zfmk.de/web/Forschung/Tagungen/2012/201202_GfBS/Workshops/ALISCORE/index.de.html).

Delirious does not infer substitution model parameters during a run. You may choose from some common built-in models (JTT, JC, etc.), or you may provide your own time-reversible model through a program option. There are programs that help you determining a suitable model, e.g., [PROTTEST](http://darwin.uvigo.es/software/prottest.html) and [jMODELTEST](http://darwin.uvigo.es/software/jmodeltest.html). The latter will report estimated substitution model parameters that can be input with option `-sm USERDEFINED='DNA;[pi1,...,pik];[r1,...,rj]'`.

### Mapping ###
The guest-to-host leaf map is a straightforward tab-delimited file, e.g., for the example above:
```
Oryctolagus_cuniculus_1	Oryctolagus_cuniculus
Cavia_porcellus_1	Cavia_porcellus
Cavia_porcellus_2	Cavia_porcellus
Mus_musculus_1	Mus_musculus
Oryctolagus_cuniculus_2	Oryctolagus_cuniculus
Cavia_porcellus_3	Cavia_porcellus
Mus_musculus_2	Mus_musculus
Equus_caballus_1	Equus_caballus
Felis_catus_1	Felis_catus
Felis_catus_2	Felis_catus
Canis_lupus_1	Canis_lupus
Monodelphis_domestica_1	Monodelphis_domestica
```
Please note that the names are case-sensitive and must match those in the host and sequence files, and also that only the actual ID of the guest sequence should be used. This is of particular importance in pipe-separated headers, meaning that for an NCBI-style FASTA header like
```
>gi|gi-number|gb|accession
```
only `accession` should appear in the mapping file, while for a header like
```
>custom|moreinfo|evenmoreinfo
```
only the initial part `custom` will be interpreted as the ID.


---


---


---



## OUTPUT ##
Output is typically directed to a file with the following option: `-o myoutput.mcmc`. If not specified, output will be written to stdout. If the file option is used, two files will be produced:

  1. `myoutput.mcmc`, containing tab-delimited samples drawn from the posterior distribution.
  1. `myoutput.mcmc.info`, containing general pre- and post-run information.

The file with the samples will look something like:
```
Iteration       UnnormalizedPosteriorDensity       SubstitutionModelDensity     DLRModelDensity     DuplicationRate LossRate        EdgeRateMean    EdgeRateCV      GuestTree
0       -3552.679327202671      -3499.5465333279344     -53.13279387473693      1.2695758513931887      1.2695758513931887      0.5     1.0     ((((((Canis_lupus_1,(Felis_catus_1,Felis_catus_2)),Equus_caballus_1),Monodelphis_domestica_1),((Cavia_porcellus_3,Oryctolagus_cuniculus_2),Mus_musculus_2)),((Cavia_porcellus_1,Mus_musculus_1),Cavia_porcellus_2)),Oryctolagus_cuniculus_1);
100     -3460.3268146346086     -3418.3402167205877     -41.98659791402091      1.382297474580881       1.9561323720461337      0.34371401944169133     0.8636497213264948      ((((((Canis_lupus_1,(Felis_catus_1,Felis_catus_2)),Equus_caballus_1),Monodelphis_domestica_1),((Cavia_porcellus_3,Oryctolagus_cuniculus_2),Mus_musculus_2)),((Cavia_porcellus_1,Mus_musculus_1),Cavia_porcellus_2)),Oryctolagus_cuniculus_1);
200     -3377.8681987196874     -3355.0176933757098     -22.85050534397761      1.9884471995654707      2.2021552001268927      0.2527456690787075      0.5533612995885099      (((((Canis_lupus_1,(Felis_catus_1,Felis_catus_2)),Equus_caballus_1),Monodelphis_domestica_1),((Cavia_porcellus_3,Oryctolagus_cuniculus_2),Mus_musculus_2)),((Cavia_porcellus_1,Mus_musculus_1),(Cavia_porcellus_2,Oryctolagus_cuniculus_1)));
300     -3374.107762944613      -3353.653784061512      -20.453978883101296     0.6946280180733413      1.7153266079409586      0.2597670645491367      0.5326983810097781      (((((Canis_lupus_1,(Felis_catus_1,Felis_catus_2)),Equus_caballus_1),Monodelphis_domestica_1),((Cavia_porcellus_3,Oryctolagus_cuniculus_2),Mus_musculus_2)),((Cavia_porcellus_1,Mus_musculus_1),(Cavia_porcellus_2,Oryctolagus_cuniculus_1)));
400     -3364.1989247177135     -3347.7627052940393     -16.436219423674128     0.20484065463728496     1.2600320636943336      0.2780947853756359      0.6196617091721438      (((((Canis_lupus_1,(Felis_catus_1,Felis_catus_2)),Equus_caballus_1),Monodelphis_domestica_1),((Cavia_porcellus_3,Mus_musculus_2),Oryctolagus_cuniculus_2)),((Cavia_porcellus_1,Mus_musculus_1),(Cavia_porcellus_2,Oryctolagus_cuniculus_1)));
...
```

The `PosteriorDensity` column refers to the unnormalized posterior density in log-format, and corresponds to the products of the likelihood of sub-models and priors. The duplication rate and other model parameters refer to the rescaled version of the host tree. The guest tree topology is output as a sorted Newick string without branch lengths. To include a tree with branch lengths, one may specify option `-lout`.

The info file will look something like:
```
# =========================================================================
# ||                             PRE-RUN INFO                            ||
# =========================================================================
# DELIRIOUS
# Arguments: [-o, example1.mcmc, -i, 10000, example1.stree, example1.fa, example1.map]
# Current time: 2012-07-10 11:36:33
# Host tree rescaling factor: 0.0061012812690665035
# Initial guest tree: Produced with NJ on sequence identity (arbitrarily rooted).
# MCMC manager:
#       MCMC MANAGER
#       Psuedo-random number generator:
#               MERSENNE-TWISTER PRNG
#               Seed as integer: -165791062564516345067622441299051870177
#               Seed as byte-array: [-125, 69, -51, -124, -20, -62, 8, -53, -73, 18, -97, 40, -69, -81, 8, 31]
#
... 
# =========================================================================
# ||                             POST-RUN INFO                           ||
# =========================================================================
# DELIRIOUS
# MCMC manager:
#       MCMC MANAGER
#       Wall time: 16793658000 ns = 16,79 s = 0,28 min = 0 h
...
#       Statistics:
#               FINE-DETAILED PROPOSER STATISTICS
#               Acceptance ratio: 2673 / 10000 = 0.2673
#               Acceptance ratios per window:
#                       1       730 / 2498 = 0.2922337870296237
#                       2       666 / 2500 = 0.2664
#                       3       706 / 2500 = 0.2824
#                       4       686 / 2500 = 0.2744
#                       5       630 / 2500 = 0.252
#                       6       672 / 2500 = 0.2688
#                       7       594 / 2500 = 0.2376
#                       8       662 / 2502 = 0.26458832933653076
#               Acceptance ratios for sub-categories:
#                       1 used proposers        2248 / 6940 = 0.3239193083573487
#                       2 used proposers        321 / 2043 = 0.15712187958883994
#                       3 used proposers        104 / 1017 = 0.10226155358898721
```

Notably, at the end of the info file, you can find statistics for acceptance ratios which may be needed for tuning to obtain proper mixing of the MCMC chain (see below).


---


---


---



## RUNNING ##

### Starting the application ###
Delirious is started as a command-line Java application. It is possible that future versions will come with a graphical user interface, a command-line based environment for managing options, or similarly. However, at the moment, you would start Delirious by running e.g.:
```
java -jar jprime-X.Y.Z.jar Delirious [options] <host tree> <msa> <leaf map>
```
where the JAR file of course refer to your current setup. See also the section on options below.
You may need to increase the default heap size allocated by Java. The memory requirements will primarily depend on the size of your gene family, MSA length and substitution model. We recommend using Oracle's Java HotSpot virtual machine (VM), in which case a recommended heap size of 512 MB and a maximum heap size of 1024 MB would be specified with:
```
java -Xms512m -Xmx1024m -jar jprime-X.Y.Z.jar Delirious [options] <host tree> <msa> <leaf map>
```
Note that these options refer to Java's VM itself, and are not options of Delirious. Tuning other aspects of a Java VM is a non-trivial area and is probably not necessary. Should you still wish to do so, please have a look at these documents: [Oracle Java HotSpot VM FAQ](http://www.oracle.com/technetwork/java/hotspotfaq-138619.html) and [Oracle Java HotSpot VM Options](http://www.oracle.com/technetwork/java/javase/tech/vmoptions-jsp-140102.html).

### Pilot runs ###
Before performing your real runs, you should make pilot runs to assess the MCMC mixing and change tuning parameters accordingly. The length of a pilot run needs typically not be as long as a final run. Try striving for parameter acceptance ratios of 0.2 - 0.5 (preferably in the lower region). Even so, around 0.1 - 0.7 might do the trick on smaller trees. For the topology, the acceptance ratios will typically be much lower, and cannot really be tuned. The acceptance ratios can be inspected in the info file. For larger trees, consider increasing the weight for how often tree moves are carried out. See the example and option sections below for more information on how to set the tuning parameters.

### Final runs ###
For your final runs, the number of iterations required and a suitable thinning factor will depend on the size of your input. No exact numbers can be provided; you will have to rely on "common sense", and analyses (see below) ;-)  You probably want to perform 2 - 4 independent "parallel" MCMC chains to be able to conduct proper convergence diagnostics.


---


---


---



## ANALYSIS ##
As in most MCMC inference tools, you will need to verify that the chain has been mixing properly and reached convergence to the posterior distribution. However, ultimately, you should be rewarded with obtaining the full probabilistic picture, and not just a point sample.

You should discard an initial proportion of the samples as burn-in. Convergence statistics may assist you in determining how many. Such measures typically include Gelman-Rubin diagnostics, effective sample size (ESS), potential scale reduction factor (PSRF), sample autocorrelation, and manual inspection of the agreement of log-likelihoods, trees and parameter estimates for parallel chains.

### Convergence diagnostics and analysis tools ###

Several MCMC-tailored analysis softwares exist, among them:
  * The [CRAN R](http://cran.r-project.org) package [CODA](http://cran.r-project.org/web/packages/coda/index.html).
  * [VMCMC](VMCMC.md). Includes many of CODA's diagnostics, but is easier to use.
  * [Tracer](http://tree.bio.ed.ac.uk/software/tracer/).
  * [AWTY](http://ceb.csit.fsu.edu/awty/). Particularly tailored for inspecting clade convergence diagnostics.

The following shows a trace in [VMCMC](VMCMC.md) of the unnormalized posterior density of a short MCMC chain, along with a potential choice of burn-in period length.

![http://jprime.googlecode.com/files/DLRS_convergence.png](http://jprime.googlecode.com/files/DLRS_convergence.png)

### "Hello world!" parameter analysis ###

Generally, CRAN R is suitable for many types of analyses. For instance, you could read and plot the duplication rate in R thus:
```
> burnInLim <- 250000;
> chain <- read.table('myoutput.mcmc', header=TRUE);
> chain <- chain[chain$Iteration > burnInLim, ];
> hist(chain$DuplicationRate, 40);
```

To get a quick glimpse of the tree space, you could even use a shell such as bash (assuming the tree is in column 9 and we only look at the last 5,000 samples):
```
$ cut -f9 myoutput.mcmc | tail -5000 | sort | uniq -c | sort -n
```
This will quickly identify the maximum probability tree among the 5,000 samples.

### Common tasks ###
In general, we have the following recommendations for some common analyses:

**Guest tree point estimate:** If you need a point estimate, pick the maximum probability tree. Unless coinciding with this tree, another candidate is the tree with the highest unnormalized posterior density, but this is often "noisier" and may just be a reasonably good tree with haphazard well-tuning of branch lengths, etc. Of course, one may find scenarios where the actual MAP tree is from a "narrow region" and is not the same as the maximum probability tree. An entirely different approach may be to obtain a consensus tree of some sort, preferably weighted by the abundance of the different input trees (i.e. the samples).

**Guest tree variance:**There are a number of potential variants here. One can, e.g., look at posterior clade probabilities, or treat the topology as a discrete variable and just look at its distribution or Shannon entropy. A different approach is to compute a tree distance metric such as Robinson-Foulds, which can be visualized with, for instance, multidimensional scaling (MDS) techniques.

**Continuous parameter point estimate:** We recommend obtaining a smoothed _maximum a posteriori_ (MAP) estimate. The reason for this is that the distribution is often highly skewed by being bounded at (and close to) zero. It may be advisable to perform some sort of boundary correction such as mirroring when smoothing to actually find MAP values at zero, since the sampling is sparser there. The mean or median is an alternative, if the distribution is not too skewed and pronounced at zero. In this case, consider doing some outlier filtering, such as removing values based on the interquartile range (IQR). Note that the event rates are typically highly correlated with each other and with the guest tree topology. It may therefore be reasonable to estimate them jointly.

**Continuous parameter variance:** This is straightforward. Typically, the variance, standard deviation or coefficient of variation suffices. However, consider doing some outlier filtering first, as above.


---


---


---


## OPTIONS ##
Delirious comes with a large number of user options; please don't feel overwhelmed! Some of the more commonly used ones are detailed below. You can always type
```
java -jar jprime-X.Y.Z.jar Delirious -h
```
to get a more up-to-date description of all available options.

**General options**
  * `-o <string>  `  Output file for MCMC chain. The auxiliary info file will be appended with .info.
  * `-i <int>  `  Number of iterations, e.g. 1000000.
  * `-t <int>  ` Governs how often samples are taken, e.g. 200.
  * `-lout  ` To include a Newick tree with branch lengths among the samples and not only the plain Newick tree.

**Substitution model**

Delirious can work with DNA, peptide and codon MSAs. As of yet, working with several independent loci is not supported.
  * `-sm <string>  ` The substitution model. Some common empirical models are included. However, you may also provide your own time-reversible model by inputting its stationary frequencies and its exchangeability matrix (not the transition matrix).
  * `-srcats <int>  ` Number of discrete site rate categories for modeling gamma site rate heterogeneity. The default is 1 category. Note: Running time may in many cases grow linearly with the number of categories. Invariant sites are not modeled.
  * `-erpd <string>  ` Probability distribution used for modeling iid rates of relaxed molecular clock. The default is the gamma distribution.

**Initial values and leaving parameters out**

  * `-g <string>  ` Initial guest tree. You can choose to create a random tree, a NJ tree based on sequence identity, or inputting your own tree, possibly with branch lengths. Recently, support for inferring a more sophisticated NJ tree was added with option `-fp`. Note: The guest tree must be rooted and bifurcating.
  * `-gfix  ` Fix the guest tree.
  * `-lfix  ` Fix the branch lengths.
  * `-dup  <float>  ` Duplication rate. You may fix this value by appending it with FIXED.
  * `-loss  <float>  ` Loss rate. Fix as above.
  * `-erpdm  <float>  ` Relaxed molecular clock mean. Fix as above.
  * `-erpdcv  <float>  ` Relaxed molecular clock coefficient of variation. Fix as above.
  * `-srshape <float>  ` Gamma site rate shape parameter. Fix as above.

**Tuning parameters**

In order to get good mixing, it might be necessary to tweak the tuning parameters. These come in several flavours, of which the most important ones are for 1) setting the audacity of the proposed new states, and 2) setting how often a state parameter should be perturbed.
  * `-tng<param> <[float,float]>  ` General pattern for tuning parameters of floating point state parameters. The specified values controls the proposal distribution's CV for the first and last iterations respectively. Try striving for acceptance ratios of around 0.2-0.5. To see whether this is the case, inspect the info files of pilot runs. If acceptance ratio too low, decrease the CV from its default, if too high, increase the CV.
  * `-tngw<param> <[float,float]>  ` General pattern for setting the weights for how often parameters are perturbed with respect to each other.

**Discretization**

Should the default discretization settings not be satisfactory, you can change them. A denser discretization means higher precision but also higher running time. See the figure above for a more detailed description.
  * `-dts <float>  ` Approximate timestep between discretization points. Remember that this refers to the rescaled species tree where the root-to-leaf time is 1.
  * `-dmin <int>  ` The minimum number of discretization slices of an edge.
  * `-dmax <int>  ` The maximum number of discretization slices of an edge.
  * `-dstem <int>  ` The number of discretization slices of the stem edge. Too many will inflate the running time, too few may cause the gene tree to not fit when trying out different topologies in the MCMC.



---


---


---



## EXAMPLES ##
### 1: Synthetic gene family ###
A synthetic mammal protein family (the same as used in the tutorial above).

  * [dlrs\_example\_1.stree](http://jprime.googlecode.com/files/dlrs_example_1.stree) (host tree)
  * [dlrs\_example\_1.fa](http://jprime.googlecode.com/files/dlrs_example_1.fa) (protein MSA)
  * [dlrs\_example\_1.map](http://jprime.googlecode.com/files/dlrs_example_1.map) (guest-to-host leaf map)

Fancying 1,000,000 iterations, sampling every 200th iteration, and using the JTT substitution model, we may get something like:
```
java -Xms128m -Xmx256m -jar jprime-X.Y.Z.jar Delirious -o dlrs_example_1.mcmc -i 1000000 -t 200 -sm JTT dlrs_example_1.stree dlrs_example_1.fa dlrs_example_1.map
```

### 2: Yeast orthologs ###

Family no. 1,975 from the [Yeast Gene Order Browser (YGOB)](http://wolfe.gen.tcd.ie/ygob/).

  * [dlrs\_example\_2.stree](http://jprime.googlecode.com/files/dlrs_example_2.stree) (host tree)
  * [dlrs\_example\_2.fa](http://jprime.googlecode.com/files/dlrs_example_2.fa) (protein MSA)
  * [dlrs\_example\_2.map](http://jprime.googlecode.com/files/dlrs_example_2.map) (guest-to-host leaf map)
  * [dlrs\_example\_2.gtree](http://jprime.googlecode.com/files/dlrs_example_2.gtree) (guest tree)

If we hypothesize that we have only orthologs, save for a predicted WGD, and we wish to fix this tree and output branch lengths with the samples, we might execute:
```
java -Xms128m -Xmx256m -jar jprime-X.Y.Z.jar Delirious -o dlrs_example_2.mcmc -i 100000 -t 100 -sm JTT -g dlrs_example_2.gtree -gfix -lout dlrs_example_2.stree dlrs_example_2.fa dlrs_example_2.map
```

### 3: ABCA subfamily ###
The ABCA subfamily for some vertebrates. 12 gene members in human.

  * [dlrs\_example\_3.stree](http://jprime.googlecode.com/files/dlrs_example_3.stree) (host tree)
  * [dlrs\_example\_3.fa](http://jprime.googlecode.com/files/dlrs_example_3.fa) (protein MSA trimmed of uninformative positions)
  * [dlrs\_example\_3.map](http://jprime.googlecode.com/files/dlrs_example_3.map) (guest-to-host leaf map)

On this larger family, we might want to model site rate heterogeneity, achieve some more reasonable acceptance rates by changing the tuning parameters based on pilot runs, and increase the how often the guest tree topology and the branch lengths are perturbed:
```
java -Xms768m -Xmx1536m -jar jprime-X.Y.Z.jar Delirious -i 1000000 -t 200 -o dlrs_example_3.mcmc -lout -sm JTT -srcats 4 -tngdup [0.3,0.3] -tngloss [0.3,0.3] -tngerm [0.1,0.1] -tngercv [0.1,0.1] -tngl [0.4,0.4] -tngsrshape [0.07,0.07] -tngwg [3.0,3.0] -tngwl [15.0,15.0] dlrs_example_3.stree dlrs_example_3.fa dlrs_example_3.map
```

Note: This will be a long and time-consuming MCMC chain.



---


---


---


## FAQ ##

**Q1) What kind of trees and tree formats does Delirious support?**

A1) Delirious uses strictly bifurcating rooted trees. This goes for both host and guest tree, and any input must comply with this. At the moment only input trees on the Newick format are supported.

**Q2) What kind of MSA formats does Delirious support?**

A2) At the moment, only the FASTA format is supported.

**Q3) I get errors that seem to be related to the guest or species names. What could be wrong?**

A3) Make sure that the guest-to-host mapping file refers precisely to the sequence names and host tree leaves (they are case sensitive). Also, please note that the mapping file should only reference guest sequences by IDs (typically accessions). See the input instructions above for more info. If '|' characters in the FASTA header is still causing issues, try replacing them with e.g. '`_`' in the MSA file and the mapping file.

**Q4) I get an error about "Division with zero" when I try to start Delirious. What's wrong?**

A4) This means that the initial state is so poor that its probability gets rounded to zero. You might want to check these things:
  1. Do you have a reasonable time for the stem edge? We recommend somewhere between 20%-100% of the root-to-leaf time. Check that this isn't set orders of magnitude smaller.
  1. Is there a user-defined initial guest tree that includes branch lengths? If so, do they agree with the substitution model? If not, try removing the lengths to start with simple default values.
  1. Perhaps the initial values for the duplication or loss rate are inappropriate. Try setting some user-defined initial values.
  1. Perhaps the initial guest tree is too unrealistic. Try estimating a better topology, either by option -g NJ or option -fp. Perhaps try to infer a tree with branch lengths using some external method, such as MEGA.

**Q5) Can I easily get a hold of divergence times for the host tree?**

A5) That depends. Reasonable estimates can often be obtained from the excellent initiative http://www.timetree.org. If these are missing or seem unreasonable due to a large variance, you will have to date a tree yourself using one of the methods mentioned in the instructions above, preferably using a clock relaxation technique similar to what you use in Delirious, e.g., _iid_ gamma edge rates.

**Q6) Does Delirious support GTR?**

A6) No, not at this point. Only empirical and fixed-parameter substitution models are included. You may, however, make an _a priori_ estimate of such parameters using e.g. [jMODELTEST](http://darwin.uvigo.es/software/jmodeltest.html), and then feeding them into Delirious.

**Q7) Does Delirious support separate partitions of MSA data corresponding to multiple loci (having separate substitution models / parameters)?**

A7) No, not yet, although this may be included in the future.

**Q8) What is reasonable for the stem edge timespan and discretisation density?**

A8) The timespan of the stem edge should be set to some reasonable value, e.g. 25-150% of the root-to-leaf-time. If you expect multiple out-paralogs to occur prior to the species tree root, then the upper range should probably be used. Having a long stem edge timespan will reduce the duplication-loss rates slightly, although this is often marginal in consideration of the overall timespan of the species tree. As for the discretization of the stem edge, the default value ought to do the trick in most cases. However, you may need to increase it if it causes issues during the run, or decrease it if the running time seems too burdensome. The number of points (specified through option `-dstem n`) must be sufficient for accomodating strange guest trees when moving  through tree space, but should ideally be as small as possible to reduce running time.

**Q9) Does Delirious support parallelization?**

A9) Not at the moment. It is possible that future versions will include support for multiple cores (in particular when Java 7+ has become sufficiently adopted). MPI support for multiple compute nodes seems more unlikely. In such a scenario, we might opt instead for inclusion of a Hadoop/Map-Reduce framework

**Q10) Does Delirious support Markov-coupled MCMC (MC^3)?**

A10) Not at the moment. We are, however, pursuing efforts of improving exploration of the tree space for large trees.

**Q11) I'm providing my own initial guest tree, but it seems to cause issues. Why?**

A11) As stated above, the guest tree must be rooted and provided on the Newick format. Many applications will generate unrooted trees such as "(A,B,C)". In this case make a qualified guess, or simply pick an arbitrary root such as "((A,B),C)". If the tree contains branch lengths, it might be easiest just to remove them first.

**Q12) Why do I get an error about "Compound not found" when I try to start the application?**

A12) You probably have peptide data but are using a nucleotide substitution model. Set a suitable model with option -sm.