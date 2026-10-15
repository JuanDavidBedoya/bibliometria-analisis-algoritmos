package com.uniquindio.bibliometria.algorithms.similarity.classic;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/** Explicit set construction, intersection and union. No library similarity function. */
public final class JaccardSimilarity {
    public enum EmptyUnionPolicy { REJECT, ZERO, ONE }

    public record Result(List<String> leftSet, List<String> rightSet,
                         List<String> intersection, List<String> union, double similarity) {
        public Result {
            leftSet = List.copyOf(leftSet);
            rightSet = List.copyOf(rightSet);
            intersection = List.copyOf(intersection);
            union = List.copyOf(union);
        }
    }

    public Result compare(List<String> left, List<String> right, EmptyUnionPolicy policy) {
        Objects.requireNonNull(policy, "policy");
        Set<String> a = unique(left);
        Set<String> b = unique(right);
        Set<String> intersection = new LinkedHashSet<>();
        Set<String> union = new LinkedHashSet<>();
        for (String token : a) {
            union.add(token);
            if (b.contains(token)) intersection.add(token);
        }
        for (String token : b) union.add(token);
        double score;
        if (union.isEmpty()) {
            if (policy == EmptyUnionPolicy.REJECT) {
                throw new IllegalArgumentException("Jaccard has an empty union; choose an explicit policy");
            }
            score = policy == EmptyUnionPolicy.ONE ? 1.0 : 0.0;
        } else {
            score = (double) intersection.size() / union.size();
        }
        return new Result(List.copyOf(a), List.copyOf(b), List.copyOf(intersection),
                List.copyOf(union), score);
    }

    private Set<String> unique(List<String> tokens) {
        Objects.requireNonNull(tokens, "tokens");
        Set<String> result = new LinkedHashSet<>();
        for (String token : tokens) {
            Objects.requireNonNull(token, "token");
            if (token.isBlank()) throw new IllegalArgumentException("Blank tokens are not allowed");
            result.add(token);
        }
        return result;
    }
}
