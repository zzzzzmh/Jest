package io.searchbox.core;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.gson.Gson;
import io.searchbox.action.AbstractAction;
import io.searchbox.action.AbstractMultiTypeActionBuilder;
import io.searchbox.action.GenericResultAbstractAction;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

/**
 * @author Dogukan Sonmez
 * @author cihat keser
 */
public class MultiGet extends GenericResultAbstractAction {

    private Object source;

    protected MultiGet(AbstractAction.Builder builder) {
        super(builder);
        setBulkOperation(true);
        setURI(buildURI());
        setPathToResult("docs/_source");
    }

    public MultiGet(Builder.ByDoc builder) {
        this((AbstractAction.Builder) builder);
        this.source = ImmutableMap.of("docs", docsToMaps(builder.docs));
    }

    public MultiGet(Builder.ById builder) {
        this((AbstractAction.Builder) builder);
        this.source = ImmutableMap.of("ids", builder.ids);
    }

    @Override
    public Object getData(Gson gson) {
        return gson.toJson(source);
    }

    protected Object docsToMaps(List<Doc> docs) {
        return Lists.transform(docs, new Function<Doc, Object>() {
            @Override
            public Object apply(Doc doc) {
                return doc.toMap();
            }
        });
    }

    @Override
    protected String buildURI() {
        StringBuilder sb = new StringBuilder();
        sb.append(super.buildURI()).append("/_mget");
        return sb.toString();
    }

    @Override
    public String getRestMethodName() {
        return "GET";
    }

    public static class Builder {
        private Builder() {
        }

        public static class ById extends AbstractMultiTypeActionBuilder<MultiGet, ById> {
            private List<String> ids = new LinkedList<String>();

            /**
             * The mget API allows for _type to be optional. Set it to _all or leave it empty in order to
             * fetch the first document matching the id across all types. If you don’t set the type and
             * have many documents sharing the same _id, you will end up getting only the first matching document.
             */
            public ById(String index, String type) {
                addIndex(index);
                addType(type);
            }

            public ById addId(Collection<? extends String> ids) {
                this.ids.addAll(ids);
                return this;
            }

            public ById addId(String id) {
                ids.add(id);
                return this;
            }

            @Override
            public MultiGet build() {
                return new MultiGet(this);
            }
        }

        public static class ByDoc extends GenericResultAbstractAction.Builder<MultiGet, ByDoc> {
            private List<Doc> docs = new LinkedList<Doc>();

            public ByDoc(Collection<? extends Doc> docs) {
                this.docs.addAll(docs);
            }

            public ByDoc(Doc doc) {
                docs.add(doc);
            }

            public ByDoc addDoc(Collection<? extends Doc> docs) {
                this.docs.addAll(docs);
                return this;
            }

            public ByDoc addDoc(Doc doc) {
                docs.add(doc);
                return this;
            }

            @Override
            public MultiGet build() {
                return new MultiGet(this);
            }
        }
    }
}
