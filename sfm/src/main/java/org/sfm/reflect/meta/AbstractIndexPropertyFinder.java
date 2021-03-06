package org.sfm.reflect.meta;

import org.sfm.reflect.InstantiatorDefinition;
import org.sfm.tuples.Tuple2;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class AbstractIndexPropertyFinder<T> implements PropertyFinder<T> {
    protected final ClassMeta<T> classMeta;
    protected final List<IndexedElement<T, ?>> elements;
    private final Map<String, Integer> speculativeIndexes = new HashMap<String, Integer>();

    public AbstractIndexPropertyFinder(ClassMeta<T> classMeta) {
        this.elements = new ArrayList<IndexedElement<T, ?>>();
        this.classMeta = classMeta;
    }

    @SuppressWarnings("unchecked")
    public <E> PropertyMeta<T, E> findProperty(PropertyNameMatcher propertyNameMatcher) {

        IndexedColumn indexedColumn = propertyNameMatcher.matchesIndex();

        if (indexedColumn == null) {
            indexedColumn = extrapolateIndex(propertyNameMatcher);
        }

        if (indexedColumn == null) {
            indexedColumn = speculativeMatching(propertyNameMatcher);
        }

        if (indexedColumn == null || !isValidIndex(indexedColumn)) {
            return null;
        }

        IndexedElement<T, E> indexedElement = (IndexedElement<T, E>) getIndexedElement(indexedColumn);

        if (indexedElement.getElementClassMeta().isLeaf() || indexedColumn.getSubPropertyNameMatcher() == null) {
            indexedElement.addProperty(".");
            return indexedElement.getPropertyMeta();
        }

        PropertyFinder<?> propertyFinder = indexedElement.getPropertyFinder();

        if (propertyFinder == null) {
            return null;
        }

        PropertyMeta<?, ?> subProp = propertyFinder.findProperty(indexedColumn.getSubPropertyNameMatcher());
        if (subProp == null) {
            return null;
        }

        indexedElement.addProperty(subProp);

        return new SubPropertyMeta(classMeta.getReflectionService(), indexedElement.getPropertyMeta(), subProp);
    }

    protected abstract boolean isValidIndex(IndexedColumn indexedColumn);

    protected abstract <E> IndexedElement<T,?> getIndexedElement(IndexedColumn indexedColumn);

    private IndexedColumn speculativeMatching(PropertyNameMatcher propertyNameMatcher) {
        // try to match against prefix
        Tuple2<String, PropertyNameMatcher> speculativeMatch = propertyNameMatcher.speculativeMatch();

        IndexedColumn indexedColumn = null;
        if (speculativeMatch != null) {
            Integer index = speculativeIndexes.get(speculativeMatch.first());

            if (index == null) {
                indexedColumn = extrapolateIndex(speculativeMatch.getElement1());
                if (indexedColumn != null) {
                    speculativeIndexes.put(speculativeMatch.first(), indexedColumn.getIndexValue());
                }
            } else {
                indexedColumn = new IndexedColumn(index, speculativeMatch.getElement1());
            }
        }
        return indexedColumn;
    }

    protected abstract IndexedColumn extrapolateIndex(PropertyNameMatcher propertyNameMatcher);

    @Override
    public List<InstantiatorDefinition> getEligibleInstantiatorDefinitions() {
        return classMeta.getInstantiatorDefinitions();
    }
}
