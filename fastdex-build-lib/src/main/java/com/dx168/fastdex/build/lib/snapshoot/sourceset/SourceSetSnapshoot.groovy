package com.dx168.fastdex.build.lib.snapshoot.sourceset

import com.dx168.fastdex.build.lib.snapshoot.api.DiffInfo
import com.dx168.fastdex.build.lib.snapshoot.api.DiffResultSet
import com.dx168.fastdex.build.lib.snapshoot.api.Snapshoot
import com.dx168.fastdex.build.lib.snapshoot.api.Status
import com.dx168.fastdex.build.lib.snapshoot.file.FileNode;
import com.dx168.fastdex.build.lib.snapshoot.string.BaseStringSnapshoot;
import com.dx168.fastdex.build.lib.snapshoot.string.StringDiffInfo;
import com.dx168.fastdex.build.lib.snapshoot.string.StringNode;
import com.google.gson.annotations.SerializedName

/**
 * Created by tong on 17/3/31.
 */
public final class SourceSetSnapshoot extends BaseStringSnapshoot<StringDiffInfo,StringNode> {
    public String path;//工程目录

    @SerializedName("sourceSets")
    public Set<JavaDirectorySnapshoot> directorySnapshootSet = new HashSet<>();

    public SourceSetSnapshoot() {
    }

    public SourceSetSnapshoot(SourceSetSnapshoot snapshoot) {
        super(snapshoot);
        //from gson
        directorySnapshootSet.addAll(snapshoot.directorySnapshootSet);
    }

    public SourceSetSnapshoot(File projectDir,Set<String> sourceSets) throws IOException {
        super(sourceSets);
        init(projectDir,sourceSets);
    }

    public SourceSetSnapshoot(File projectDir,String ...sourceSets) throws IOException {
        super(sourceSets);

        Set<String> set = new HashSet<>();
        for (String str : sourceSets) {
            set.add(str);
        }
        init(projectDir,set);
    }

    private void init(File projectDir,Set<String> sourceSets) throws IOException {
        if (projectDir == null || projectDir.length() == 0) {
            throw new RuntimeException("Invalid projectPath");
        }
        this.path = projectDir.getAbsolutePath();
        if (directorySnapshootSet == null) {
            directorySnapshootSet = new HashSet<>();
        }

        for (String sourceSet : sourceSets) {
            directorySnapshootSet.add(new JavaDirectorySnapshoot(new File(sourceSet)));
        }
    }

    @Override
    protected SourceSetDiffResultSet createEmptyResultSet() {
        return new SourceSetDiffResultSet();
    }

    @Override
    public DiffResultSet<StringDiffInfo> diff(Snapshoot<StringDiffInfo, StringNode> otherSnapshoot) {
        SourceSetDiffResultSet sourceSetResultSet = (SourceSetDiffResultSet) super.diff(otherSnapshoot);
        SourceSetSnapshoot oldSnapshoot = (SourceSetSnapshoot)otherSnapshoot;

        for (DiffInfo diffInfo : sourceSetResultSet.getDiffInfos(Status.DELETEED)) {
            JavaDirectorySnapshoot javaDirectorySnapshoot = oldSnapshoot.getJavaDirectorySnapshootByPath(diffInfo.uniqueKey);
            for (FileNode node : javaDirectorySnapshoot.nodes) {
                sourceSetResultSet.addJavaFileDiffInfo(new JavaFileDiffInfo(Status.DELETEED,null,node));
            }
        }

        for (DiffInfo diffInfo : sourceSetResultSet.getDiffInfos(Status.ADDED)) {
            JavaDirectorySnapshoot javaDirectorySnapshoot = getJavaDirectorySnapshootByPath(diffInfo.uniqueKey);
            for (FileNode node : javaDirectorySnapshoot.nodes) {
                sourceSetResultSet.addJavaFileDiffInfo(new JavaFileDiffInfo(Status.ADDED,node,null));
            }
        }

        for (DiffInfo diffInfo : sourceSetResultSet.getDiffInfos(Status.NOCHANGED)) {
            JavaDirectorySnapshoot now = getJavaDirectorySnapshootByPath(diffInfo.uniqueKey);
            JavaDirectorySnapshoot old = oldSnapshoot.getJavaDirectorySnapshootByPath(diffInfo.uniqueKey);

            JavaDirectoryDiffResultSet resultSet = (JavaDirectoryDiffResultSet) now.diff(old);

            println("==fastdex now: ${now.getAllNodes()}")
            println("==fastdex old: ${old.getAllNodes()}")
            println("==fastdex resultSet: ${resultSet.getAllChangedDiffInfos()}")
            sourceSetResultSet.mergeJavaDirectoryResultSet(resultSet);
        }
        return sourceSetResultSet;
    }

    //    @Override
//    public SourceSetDiffResultSet diff(Snapshoot<StringDiffInfo, StringNode> otherSnapshoot) {
//        JavaDirectorySnapshoot oldSnapshoot = (JavaDirectorySnapshoot)otherSnapshoot;
//
//
//        SourceSetDiffResultSet sourceSetResultSet = (SourceSetDiffResultSet) super.diff(otherSnapshoot);
//
//        Set<JavaDirectoryDiffResultSet> deletedDirectories = new HashSet<>();
//        for (DiffInfo diffInfo : sourceSetResultSet.getDiffInfos(Status.DELETEED)) {
//            deletedDirectories.add(otherSnapshoot)
//        }
//        return sourceSetResultSet;
//    }

    private JavaDirectorySnapshoot getJavaDirectorySnapshootByPath(String path) {
        for (JavaDirectorySnapshoot snapshoot : directorySnapshootSet) {
            if (snapshoot.path.equals(path)) {
                return snapshoot;
            }
        }
        return null;
    }

    /**
     * 工程路径是否发生变化
     * @param currentProjectDir
     * @return
     */
    public boolean isProjectDirChanged(File currentProjectDir) {
        return !currentProjectDir.getAbsolutePath().equals(path);
    }

    /**
     * 检查工程路径是否能对应上，如果对应不上使用参数指定的路径
     * @param currentProjectDir
     * @return 如果发生变化返回true，反之返回false
     */
    public boolean ensumeProjectDir(File currentProjectDir) {
        if (isProjectDirChanged(currentProjectDir)) {
            return false;
        }

        applyNewProjectDir(currentProjectDir);
        return true;
    }

    private void applyNewProjectDir(File currentProjectDir) {
        //TODO

    }


    @Override
    public String toString() {
        return "SourceSetSnapshoot{" +
                "directorySnapshootSet=" + directorySnapshootSet +
                '}';
    }
}
