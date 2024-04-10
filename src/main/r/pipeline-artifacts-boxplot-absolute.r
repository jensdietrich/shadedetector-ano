
# input is pipeline data produced by resultanalysis.anonymized.shadedetector.PipelineAnalysis
# @author jens dietrich
data <- read.csv("../../../results-10-09-2023.csv",TRUE,sep="\t")
head(data)
str(data)
tail(data)
plot(data)
label=c("query results","results\nconsolidated","no\ndependency","clones\ndetected","compilation\nsucceeds","vulnerability\nconfirmed")
par(cex.axis=0.75)
boxplot(data$queryresults,data$consolidated,data$nodependency,data$clonesdetected,data$povcompiled,data$vulconfirmed,names=label)







