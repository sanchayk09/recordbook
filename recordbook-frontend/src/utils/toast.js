import { toast } from 'react-toastify';
import 'react-toastify/dist/ReactToastify.css';

export const notifySuccess = (msg) => toast.success(msg || 'Success');
export const notifyError = (msg) => toast.error(msg || 'Something went wrong');
export const notifyInfo = (msg) => toast.info(msg || 'Info');

export default toast;
