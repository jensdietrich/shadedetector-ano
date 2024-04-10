package anonymized.shadedetector;


import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Utility to transparently merge responses. Some meta information of individual responses is lost,
 * but individual responses are stored in the cache and can be retried there.
 * @author jens dietrich
 */
public class ArtifactSearchResponseMerger {

   public static ArtifactSearchResponse merge (List<ArtifactSearchResponse> responses) {
       ArtifactSearchResponse merged = new ArtifactSearchResponse();

       // merge head -- attributes are not very important for future processing as origional
       // responses are retained in cache, this is mainly to merge the artifact sets

       ResponseHeader mergedHeader = new ResponseHeader();
       // use first , as this is mainly used to determine whether the dataset is outdated
       // -1 encode error
       mergedHeader.setQtime(responses.stream().mapToInt(r -> r.getHeader().getQtime()).reduce(Integer::min).orElse(-1));
       // positive status represents error code
       mergedHeader.setStatus(responses.stream().mapToInt(r -> r.getHeader().getStatus()).reduce(Integer::max).orElse(0));

       ResponseHeader.Parameters mergedParameters = new ResponseHeader.Parameters();
       mergedParameters.setRows(responses.stream().mapToInt(r -> r.getHeader().getParameters().getRows()).reduce(Integer::sum).orElse(0));
       mergedHeader.setParameters(mergedParameters);
       merged.setHeader(mergedHeader);

       // merge body
       ResponseBody mergedBody = new ResponseBody();
       mergedBody.setArtifacts(responses.stream().flatMap(r -> r.getBody().getArtifacts().stream()).collect(Collectors.toList()));
       mergedBody.setNumFound(responses.stream().mapToInt(r -> r.getBody().getNumFound()).reduce(Integer::min).orElse(0));
       mergedBody.setStart(responses.stream().mapToInt(r -> r.getBody().getStart()).reduce(Integer::min).orElse(0));
       merged.setBody(mergedBody);

       return merged;
   }

    /**
     * Strip out artifacts with GAVs that do not match a predicate.
     * (Not strictly to do with merging artifact search responses, but massaging them a different way.)
     * Based heavily on {@link #merge(List)}.
     * @return a reconstructed artifact search response containing only artifacts from {@code response} with GAVs satisfying {@code gavPredicate}
     */
   public static ArtifactSearchResponse filterArtifacts(ArtifactSearchResponse response, Predicate<String> gavPredicate) {
       // Header parameters
       ResponseHeader.Parameters newParameters = new ResponseHeader.Parameters();
       newParameters.setRows(response.getHeader().getParameters().getRows());

       // Header
       ResponseHeader newHeader = new ResponseHeader();
       newHeader.setQtime(response.getHeader().getQtime());
       newHeader.setStatus(response.getHeader().getStatus());
       newHeader.setParameters(newParameters);

       // Body
       ResponseBody newBody = new ResponseBody();
       newBody.setArtifacts(response.getBody().getArtifacts().stream()
               .filter(artifact -> gavPredicate.test(artifact.toString()))    // Do the actual work :-/
               .collect(Collectors.toList()));
       newBody.setNumFound(newBody.getArtifacts().size());
       newBody.setStart(response.getBody().getStart());

       ArtifactSearchResponse newResponse = new ArtifactSearchResponse();
       newResponse.setHeader(newHeader);
       newResponse.setBody(newBody);
       return newResponse;
   }

    /**
     * Convenience method to construct a fake empty search response with enough fields populated that
     * it can be merged with {@link #merge}.
     */
   public static ArtifactSearchResponse createEmpty(int numFound, int start, int rows) {
       ArtifactSearchResponse response = new ArtifactSearchResponse();

       // merge head -- attributes are not very important for future processing as origional
       // responses are retained in cache, this is mainly to merge the artifact sets

       ResponseHeader header = new ResponseHeader();
       // use first , as this is mainly used to determine whether the dataset is outdated
       // -1 encode error
       header.setQtime(Integer.MAX_VALUE);    // No effect on merge()'s min()
       header.setStatus(0);

       ResponseHeader.Parameters parameters = new ResponseHeader.Parameters();
       parameters.setRows(rows);
       header.setParameters(parameters);
       response.setHeader(header);

       // merge body
       ResponseBody body = new ResponseBody();
       body.setArtifacts(List.of());
       body.setNumFound(numFound);
       body.setStart(start);
       response.setBody(body);

       return response;
   }
}
