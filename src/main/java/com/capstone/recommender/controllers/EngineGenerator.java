package com.capstone.recommender.controllers;

import com.capstone.recommender.models.Visit;
import org.apache.mahout.cf.taste.common.TasteException;
import org.apache.mahout.cf.taste.impl.common.FastByIDMap;
import org.apache.mahout.cf.taste.impl.model.GenericDataModel;
import org.apache.mahout.cf.taste.impl.model.GenericUserPreferenceArray;

import org.apache.mahout.cf.taste.impl.neighborhood.NearestNUserNeighborhood;
import org.apache.mahout.cf.taste.impl.recommender.*;

import org.apache.mahout.cf.taste.impl.similarity.LogLikelihoodSimilarity;
import org.apache.mahout.cf.taste.model.DataModel;
import org.apache.mahout.cf.taste.model.PreferenceArray;
import org.apache.mahout.cf.taste.neighborhood.UserNeighborhood;
import org.apache.mahout.cf.taste.recommender.Recommender;
import org.apache.mahout.cf.taste.similarity.UserSimilarity;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

/**
 * @author sethwiesman on 3/27/15.
 */
public class EngineGenerator implements Runnable {
    public static EngineGenerator create(AtomicReference<List<Visit>> visitReference,
                                  AtomicReference<Map<Long, List<Long>>> recommenderReference) {
        return new EngineGenerator(visitReference, recommenderReference);
    }

    private final AtomicReference<List<Visit>> completeVisitsReference;
    private final AtomicReference<Map<Long, List<Long>>> recommenderReference;

    private EngineGenerator(AtomicReference<List<Visit>> completeVisitReference,
                                AtomicReference<Map<Long, List<Long>>> recommenderReference) {
        this.completeVisitsReference = completeVisitReference;
        this.recommenderReference = recommenderReference;
    }

    public Map<Long, List<Visit>> getVisitsByUid(List<Visit> visits) {
        return visits.stream().collect(Collectors.groupingBy(Visit::getUid));
    }

    @Override
    public void run() {
        Map<Long, List<Long>> recommendations = new HashMap<>();
        Set<Long> restaurants = new HashSet<>();
        List<Visit> visits = completeVisitsReference.get();

        for (Visit visit : visits) {
            restaurants.add(visit.getRid());
        }

        Map<Long, List<Visit>> visitsByUid = visits.stream().collect(Collectors.groupingBy(Visit::getUid));

        for (Long uid : visitsByUid.keySet()) {
            Set<Long> rids = visitsByUid.get(uid).stream().map(Visit::getRid).collect(Collectors.toSet());
            Set<Long> recs = new HashSet<>(restaurants);
            recs.removeAll(rids);

            recommendations.put(uid, new ArrayList<>(recs));
        }

        recommenderReference.set(recommendations);
    }

}
