package org.example.entity.file;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import lombok.NoArgsConstructor;

@NoArgsConstructor
public class FileNodeSerializer extends Serializer<FileNode> {

    @Override
    public void write(Kryo kryo, Output output, FileNode fileNode) {
        output.writeString(fileNode.getName());
        output.writeBoolean(fileNode.isFile());
        output.writeLong(fileNode.getFileSize());

        output.writeInt(fileNode.getChildren().size());
        for (FileNode child : fileNode.getChildren()) {
            kryo.writeObject(output, child);
        }
    }


    @Override
    public FileNode read(Kryo kryo, Input input, Class<? extends FileNode> type) {
        String name = input.readString();
        boolean isFile = input.readBoolean();
        long fileSize = input.readLong();

        FileNode fileNode = new FileNode(name, isFile);
        fileNode.setFileSize(fileSize);

        int childCount = input.readInt();
        for (int i = 0; i < childCount; i++) {
            FileNode child = kryo.readObject(input, FileNode.class);
            fileNode.addChild(child);
        }

        return fileNode;
    }
}
