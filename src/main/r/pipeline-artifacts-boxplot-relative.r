
# input is pipeline data produced by resultanalysis.anonymized.shadedetector.PipelineAnalysis
# @author jens dietrich
data <- read.csv("../../../results-10-09-2023.csv",TRUE,sep="\t")
head(data)
str(data)
tail(data)
plot(data)
label=c("results\nconsolidated","no\ndependency","clones\ndetected","compilation\nsucceeds","vulnerability\nconfirmed")
par(cex.axis=0.75)
boxplot(data$consolidated / data$queryresults,data$nodependency / data$queryresults,data$clonesdetected / data$queryresults,data$povcompiled / data$queryresults,data$vulconfirmed / data$queryresults,names=label)







