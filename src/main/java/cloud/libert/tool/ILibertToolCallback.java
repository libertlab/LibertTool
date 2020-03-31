package cloud.libert.tool;

import cloud.libert.tool.java.JInterface;
import cloud.libert.tool.java.JClassEntity;

public interface ILibertToolCallback {
    public ILibertToolCallback DefalutImpl = new ILibertToolCallback() {
        @Override
        public void onStart() {

        }

        @Override
        public void onAddEntity(JClassEntity entity) {

        }

        @Override
        public void onAddInterface(JInterface jInterface) {

        }

        @Override
        public void onApiDocumentCreated(String documentPath) {

        }

        @Override
        public void onEnd() {

        }
    };

    public void onStart();

    public void onAddEntity(JClassEntity entity);

    public void onAddInterface(JInterface jInterface);

    public void onApiDocumentCreated(String documentPath);

    public void onEnd();
}
