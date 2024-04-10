
# input is pipeline data produced by resultanalysis.anonymized.shadedetector.PipelineAnalysis
# @author jens dietrich
data <- read.csv("../../../results-10-09-2023.csv",TRUE,sep="\t")
head(data)
str(data)
tail(data)
label=c("query\nresults", "results\nconsolidated","no\ndependency","clones\ndetected","compilation\nsucceeds","vulnerability\nconfirmed")
par(cex.axis=0.75)

data2 <- data[ , c(2, 4,6,8,10,12)]
data3 <- t(data2)
cves <- data[ , c(1)]
matplot(data3, type = "l",xaxt = "n",ylab="artifact counts")
axis(1, at = 1:6, labels = label, cex.axis = 0.7)
# legend("topright", rownames(cves),col=seq_len(nn))

